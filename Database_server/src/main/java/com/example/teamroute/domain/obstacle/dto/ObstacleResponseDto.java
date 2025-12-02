package com.example.teamroute.domain.obstacle.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ObstacleResponseDto {
    private String status;
    private String message;
    private Object data;
}
