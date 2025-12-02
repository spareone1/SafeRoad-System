package com.example.teamroute.domain.obstacle.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class ObstacleRequestDto {
    private Long userId;
    private String obstacleName;
    private Double lat;
    private Double lon;
    private String time;
    private String image;
}
