package com.example.teamroute.domain.obstacle.service;

import com.example.teamroute.domain.obstacle.dto.ImageUploadResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class ImageService {

    // 저장 위치 (도커 내부 경로)
    private static final String UPLOAD_DIR = "/app/images";

    public ImageUploadResponseDto saveImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        // 폴더 생성 로직
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 파일명 생성 (충돌 방지용 시간 추가)
        String originalName = file.getOriginalFilename();

        // ✅ UTC 기준 시간 받아서 +9h (KST)로 변환
        String timestamp = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
        // 파일명에 공백이 있으면 에러 날 수 있으니 _로 치환
        String savedName = timestamp + "_" + originalName.replaceAll("\\s", "_");

        Path savePath = Paths.get(UPLOAD_DIR, savedName);

        try {
            // 파일 저장 실행
            file.transferTo(savePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }

        // 전체 URL 대신 '저장된 파일명'만 반환
        // 예시 반환값: "20251119_123000123_cat.jpg"
        return new ImageUploadResponseDto("ok", "이미지 업로드 성공", savedName);
    }
}