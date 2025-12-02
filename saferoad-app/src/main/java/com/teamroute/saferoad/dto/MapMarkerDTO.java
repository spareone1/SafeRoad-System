package com.teamroute.saferoad.dto;

import com.teamroute.saferoad.domain.ObstacleGroup.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MapMarkerDTO {
    private Long groupId;      // 모달을 띄우기 위한 그룹 ID
    private String obstacleName;
    private double lat;
    private double lon;
    private State state;       // 마커 색상을 구분하기 위한 상태 (processing, completed...)
}