import tkinter as tk
from tkinter import messagebox
import threading
from camera_app import start_system
import requests
import json

SERVER_IP = "119.205.220.118"
SERVER_PORT = "8080"
LOGIN_URL = f"http://{SERVER_IP}:{SERVER_PORT}/api/auth/login"

global_user_id = None
global_auth_token = None

def run_camera_thread():
    global global_user_id, global_auth_token
    
    if global_user_id is None or global_auth_token is None:
        messagebox.showerror("System Error", "Login information is missing. Please log in again.")
        return

    print("Starting detection thread...")
    detection_thread = threading.Thread(
        target=start_system, 
        args=(global_user_id, global_auth_token), 
        daemon=True,
        name='detection_main'
    )
    detection_thread.start()

def open_main_window(userid):
    main_window = tk.Toplevel(root)
    main_window.title("Main Menu")
    main_window.geometry("400x300")
    main_window.configure(bg="#f0f0f0")

    main_frame = tk.Frame(main_window, bg="#f0f0f0", padx=20, pady=20)
    main_frame.pack(expand=True, fill="both")
    main_frame.grid_columnconfigure(0, weight=1)

    welcome_label = tk.Label(main_frame, text=f"Welcome, {userid}!", font=("Arial", 16, "bold"), bg="#f0f0f0")
    welcome_label.grid(row=0, column=0, pady=(20, 30))

    start_button = tk.Button(
        main_frame, 
        text="Start Driving", 
        font=("Arial", 14, "bold"), 
        bg="#28a745",
        fg="white",
        command=run_camera_thread,
        height=2,
        width=20
    )
    start_button.grid(row=1, column=0, pady=(10, 30), ipady=5)

    main_window.protocol("WM_DELETE_WINDOW", root.destroy)
    root.withdraw()

def attempt_login():
    global global_user_id, global_auth_token
    
    userid_str = entry_user.get()
    password = entry_pass.get()
    
    login_button.config(state=tk.DISABLED)

    try:
        payload = {
            "userid": userid_str,
            "password": password
        }
        
        response = requests.post(LOGIN_URL, json=payload, timeout=5)
        
        if response.status_code == 200 or response.status_code == 201:
            data = response.json()
            
            actual_user_id = data.get('userId')
            auth_token = data.get('token')
            
            if actual_user_id is not None and auth_token:
                global_user_id = actual_user_id
                global_auth_token = auth_token
                
                messagebox.showinfo("Login Success", "Login successful.")
                open_main_window(userid_str)
            else:
                raise ValueError("Server response missing userId or token.")

        else:
            error_msg = response.json().get('message', f"Failed to login. Status: {response.status_code}")
            messagebox.showerror("Login Failed", error_msg)
            global_user_id = None
            global_auth_token = None
            
    except requests.exceptions.RequestException as e:
        messagebox.showerror("Connection Error", f"Could not connect to server: {e}")
    except ValueError as e:
        messagebox.showerror("Login Error", f"Invalid server data: {e}")
    except Exception as e:
        messagebox.showerror("An unexpected error occurred", str(e))
    finally:
        login_button.config(state=tk.NORMAL)

if __name__ == "__main__":
    
    root = tk.Tk()
    root.title("TeamRoute - Login")
    root.geometry("400x300")
    root.configure(bg="#f0f0f0")

    main_frame = tk.Frame(root, bg="#f0f0f0", padx=20, pady=20)
    main_frame.pack(expand=True, fill="both")

    title_label = tk.Label(main_frame, text="TeamRoute Login", font=("Arial", 20, "bold"), bg="#f0f0f0")
    title_label.grid(row=0, column=0, columnspan=2, pady=(10, 20))

    label_user = tk.Label(main_frame, text="User ID:", font=("Arial", 12), bg="#f0f0f0")
    label_user.grid(row=1, column=0, sticky="e", padx=10, pady=10) 
    
    entry_user = tk.Entry(main_frame, font=("Arial", 12), width=20)
    entry_user.grid(row=1, column=1, sticky="w", padx=10, pady=10)

    label_pass = tk.Label(main_frame, text="Password:", font=("Arial", 12), bg="#f0f0f0")
    label_pass.grid(row=2, column=0, sticky="e", padx=10, pady=10)
    
    entry_pass = tk.Entry(main_frame, show="*", font=("Arial", 12), width=20)
    entry_pass.grid(row=2, column=1, sticky="w", padx=10, pady=10)

    login_button = tk.Button(main_frame, text="Login", 
                            font=("Arial", 12, "bold"), 
                            bg="#007bff",
                            fg="white", 
                            command=attempt_login,
                            width=15)
    login_button.grid(row=3, column=0, columnspan=2, pady=(20, 10), ipady=5)

    main_frame.grid_columnconfigure(0, weight=1)
    main_frame.grid_columnconfigure(1, weight=1)

    root.mainloop()
