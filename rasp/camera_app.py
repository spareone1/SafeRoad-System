import cv2
from picamera2 import Picamera2
from ultralytics import YOLO
import time
from datetime import datetime
import requests
import serial
import pynmea2
import numpy as np
import io
import threading
import queue
import math
import os
import json

SERVER_IP = "119.205.220.118"
SERVER_PORT = "8080"
IMAGE_UPLOAD_URL = f"http://{SERVER_IP}:{SERVER_PORT}/api/upload-image"
JSON_REPORT_URL = f"http://{SERVER_IP}:{SERVER_PORT}/api/obstacles"

GPS_PORT = "/dev/ttyUSB0"

CURRENT_USER_ID = None
AUTH_TOKEN = None
COMMON_HEADERS = {}
session = requests.Session()

SEND_COOLDOWN = 3.0
MIN_MOVE_DIST = 1.0
RETRY_DELAY = 5.0

SHOW_DISPLAY = True

upload_queue = queue.Queue()


current_gps_data = {"lat": None, "lon": None}
gps_lock = threading.Lock()

last_send_times = {}
last_send_coords = {}


def haversine(lat1, lon1, lat2, lon2):
    if lat1 is None or lat2 is None: return 999999.0
    
    R = 6371000
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    a = math.sin(delta_phi / 2.0) ** 2 + \
        math.cos(phi1) * math.cos(phi2) * \
        math.sin(delta_lambda / 2.0) ** 2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    return R * c


def gps_thread_worker():
    global current_gps_data
    
    while True:
        try:
            with serial.Serial(GPS_PORT, 9600, timeout=1) as ser:
                while True:
                    try:
                        line = ser.readline().decode('ascii', errors='replace').strip()
                        if line.startswith('$GPGGA'):
                            msg = pynmea2.parse(line)
                            if msg.latitude != 0.0 and msg.gps_qual > 0:
                                with gps_lock: 
                                    current_gps_data["lat"] = msg.latitude
                                    current_gps_data["lon"] = msg.longitude
                    except pynmea2.ParseError:
                        continue
                    except Exception:
                        break  
        except Exception as e:
            print(f"[GPS Error] Connection failed: {e}")
            time.sleep(5)

def upload_worker():
    global CURRENT_USER_ID

    while True:
        try:
            if not session.headers.get('Authorization'):
                time.sleep(1)
                continue

            try:
                item = upload_queue.get(timeout=0.5) 
            except queue.Empty:
                continue

            if item is None: break
            
            frame, obstacle_name, lat, lon, timestamp = item
            
            try:
                _, buffer = cv2.imencode('.jpg', frame, [int(cv2.IMWRITE_JPEG_QUALITY), 70])
                image_bytes = io.BytesIO(buffer)
                files = {'file': (f'{timestamp}.jpg', image_bytes, 'image/jpeg')}
                
                res_img = session.post(IMAGE_UPLOAD_URL, files=files, timeout=5)
                
                if res_img.status_code not in [200, 201]:
                    raise Exception(f"Image Status: {res_img.status_code}")

                path = res_img.json().get('imagePath')
                
                payload = {
                    'userId': CURRENT_USER_ID, 
                    'obstacleName': obstacle_name,
                    'lat': lat, 'lon': lon, 'time': timestamp, 'image': path
                }
                res_json = session.post(JSON_REPORT_URL, json=payload, timeout=3)
                
                if res_json.status_code in [200, 201]:
                    print(f"[Success] '{obstacle_name}' uploaded (User: {CURRENT_USER_ID}).")
                else:
                    raise Exception(f"JSON Status: {res_json.status_code}")

            except Exception as e:
                print(f"[Upload Failed] Data Dropped (Network Error): {e}")
                time.sleep(RETRY_DELAY)
            
            finally:
                upload_queue.task_done()

        except Exception as main_e:
            print(f"[Worker Critical Error] {main_e}")
            time.sleep(RETRY_DELAY)

def start_system(user_id, auth_token):
    global CURRENT_USER_ID, AUTH_TOKEN, COMMON_HEADERS
    
    CURRENT_USER_ID = user_id
    AUTH_TOKEN = auth_token
    COMMON_HEADERS = {
        'Authorization': f'Bearer {AUTH_TOKEN}',
        'Connection': 'keep-alive'
    }
    session.headers.update(COMMON_HEADERS)
    
    print(f">> System Started for User ID: {CURRENT_USER_ID}")
    
    print(">> Loading YOLO model...")
    
    model = YOLO('best2.pt')
    
    picam2 = Picamera2()
    config = picam2.create_preview_configuration(main={"format": 'RGB888', "size": (1280, 720)})
    picam2.configure(config)
    picam2.start()
    
    if not any(t.name == 'gps_worker' for t in threading.enumerate()):
        threading.Thread(target=gps_thread_worker, daemon=True, name='gps_worker').start()
    if not any(t.name == 'upload_worker' for t in threading.enumerate()):
        threading.Thread(target=upload_worker, daemon=True, name='upload_worker').start()

    IMG_W, IMG_H = 1280, 720
    road_roi_points = np.array([
        (0, IMG_H), (IMG_W, IMG_H), 
        (int(IMG_W*0.7), int(IMG_H*0.4)), (int(IMG_W*0.3), int(IMG_H*0.4))
    ], np.int32)

    frame_count = 0
    cached_boxes = [] 

    print(">> System Started (Optimized Mode - No Cache)")

    try:
        while True:
            with gps_lock:
                last_known_lat = current_gps_data["lat"]
                last_known_lon = current_gps_data["lon"]

            frame = picam2.capture_array()
            frame = frame[:-16, :]
            
            detected_threat = None 
            if frame_count % 3 == 0:
                results = model(frame, verbose=False, imgsz=320)
                cached_boxes = []
                
                for r in results:
                    for box in r.boxes:
                        x1, y1, x2, y2 = map(int, box.xyxy[0])
                        cls_name = model.names[int(box.cls)]
                        
                        cx, cy = (x1+x2)//2, y2
                        if cv2.pointPolygonTest(road_roi_points, (cx, cy), False) >= 0:
                            cached_boxes.append((x1, y1, x2, y2, cls_name, float(box.conf)))
                            detected_threat = cls_name

            if SHOW_DISPLAY:
                cv2.polylines(frame, [road_roi_points], True, (255, 0, 0), 2)
                for (x1, y1, x2, y2, label, conf) in cached_boxes:
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
                    cv2.putText(frame, f"{label}", (x1, y1-5), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)

            current_time = time.time()
            if detected_threat:
                last_time = last_send_times.get(detected_threat, 0)
                last_loc = last_send_coords.get(detected_threat, (None, None))
                
                time_passed = (current_time - last_time > SEND_COOLDOWN)
                
                dist_passed = True
                if last_loc[0] is not None and last_known_lat is not None:
                    dist = haversine(last_known_lat, last_known_lon, last_loc[0], last_loc[1])
                    if dist < MIN_MOVE_DIST:
                        dist_passed = False
                
                if time_passed and dist_passed:
                    final_lat = last_known_lat
                    final_lon = last_known_lon
                    
                    if final_lat is not None:
                        ts = datetime.now().isoformat()
                        upload_queue.put((frame.copy(), detected_threat, final_lat, final_lon, ts))
                        
                        last_send_times[detected_threat] = current_time
                        last_send_coords[detected_threat] = (final_lat, final_lon)
                        
                        print(f"[Detected] {detected_threat} -> Queued (GPS: {final_lat}, {final_lon})")

            if SHOW_DISPLAY:
                cv2.imshow("Monitoring", frame)
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    break
            
            frame_count += 1

    except KeyboardInterrupt:
        print("Stopping...")
    finally:
        if SHOW_DISPLAY:
            cv2.destroyAllWindows()
        picam2.stop()
