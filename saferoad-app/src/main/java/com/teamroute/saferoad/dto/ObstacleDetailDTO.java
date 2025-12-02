package com.teamroute.saferoad.dto;

import com.teamroute.saferoad.domain.Obstacle;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 모달 상세보기를 위한 Obstacle 1개의 DTO
 */
@Getter
public class ObstacleDetailDTO {

    private Long id;
    private String obstacleName;
    private double lat;
    private double lon;
    private LocalDateTime createdAt;
    private String imageloc; // 이미지 S3 URL

    // Obstacle 엔티티를 DTO로 변환하는 생성자
    public ObstacleDetailDTO(Obstacle obstacle) {
        this.id = obstacle.getId();
        this.obstacleName = obstacle.getObstacleName();
        this.lat = obstacle.getLat();
        this.lon = obstacle.getLon();

        this.createdAt = obstacle.getCreated_at();

        this.imageloc = obstacle.getImageloc();
    }
}