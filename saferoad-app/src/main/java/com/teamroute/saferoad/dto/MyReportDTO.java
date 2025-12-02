package com.teamroute.saferoad.dto;

import com.teamroute.saferoad.domain.Obstacle;
import com.teamroute.saferoad.domain.ObstacleGroup;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyReportDTO {
    private Long id;             // 장애물 ID
    private String obstacleName; // 장애물 이름
    private String location;     // 위치 (그룹)
    private LocalDateTime createdAt; // 신고 일시
    private String imageloc;     // 사진 경로
    private ObstacleGroup.State state; // 처리 상태

    public MyReportDTO(Obstacle obstacle) {
        this.id = obstacle.getId();
        this.obstacleName = obstacle.getObstacleName();
        this.createdAt = obstacle.getCreated_at();
        this.imageloc = obstacle.getImageloc();

        // 그룹 정보가 있을 때만 가져옴
        if (obstacle.getObstaclegroup() != null) {
            this.location = obstacle.getObstaclegroup().getLocation();
            this.state = obstacle.getObstaclegroup().getState();
        } else {
            this.location = "위치 정보 없음";
            this.state = ObstacleGroup.State.non_processed;
        }
    }
}