package com.example.teamroute.domain.obstacle.controller;

import com.example.teamroute.domain.obstacle.dto.ImageUploadResponseDto;
import com.example.teamroute.domain.obstacle.dto.ObstacleRequestDto;
import com.example.teamroute.domain.obstacle.dto.ObstacleResponseDto;
import com.example.teamroute.domain.obstacle.service.ImageService;
import com.example.teamroute.domain.obstacle.service.ObstacleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // ★ Logger 사용을 위해 추가
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j // ★ Logger 사용을 위한 애너테이션 추가
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ObstacleApiController {

    private final ImageService imageService;
    private final ObstacleService obstacleService;

    // ✅ 이미지 업로드
    @PostMapping("/upload-image")
    public ResponseEntity<ImageUploadResponseDto> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(imageService.saveImage(file));
    }

    // ✅ 장애물 등록
    @PostMapping("/obstacles")
    public ResponseEntity<ObstacleResponseDto> createObstacle(@RequestBody ObstacleRequestDto dto) {

        // ★★★ RASPBERRY PI 요청 데이터 로깅 추가 부분 ★★★
        log.info("--------------------------------------------------");
        log.info("<< RPI OBSTACLE REPORT RECEIVED >>");
        // DTO 객체 자체를 출력하여 모든 필드(특히 userId)의 널(null) 여부 확인
        log.info("Received DTO: {}", dto);
        log.info("Checked UserId: {}", dto.getUserId());
        log.info("--------------------------------------------------");
        // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

        return ResponseEntity.ok(obstacleService.saveObstacle(dto));
    }

    // ✅ 장애물 목록 조회
    @GetMapping("/obstacles")
    public ResponseEntity<?> listObstacles() {
        return ResponseEntity.ok(obstacleService.getAllObstacles());
    }
}