package com.teamroute.saferoad.dto;

import com.teamroute.saferoad.domain.ObstacleGroup;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 모달 상세보기를 위한 ObstacleGroup DTO
 * (내부에 ObstacleDetailDTO 목록을 포함)
 */
@Getter
public class ObstacleGroupDetailDTO {

    private Long id;
    private String location;
    private String state;

    // 연관된 Obstacle 목록
    private List<ObstacleDetailDTO> obstacles;

    // ObstacleGroup 엔티티를 DTO로 변환하는 생성자
    public ObstacleGroupDetailDTO(ObstacleGroup group) {
        this.id = group.getId();
        this.location = group.getLocation();
        this.state = group.getState().name(); // Enum을 String으로 변환

        // Obstacle 엔티티 리스트를 ObstacleDetailDTO 리스트로 변환.
        // group.getObstacles()가 null일 경우를 대비해 null 체크 추가.
        if (group.getObstacles() != null) {
            this.obstacles = group.getObstacles().stream()
                    .map(ObstacleDetailDTO::new)
                    .collect(Collectors.toList());
        } else {
            this.obstacles = List.of(); // 빈 리스트로 초기화
        }
    }
}