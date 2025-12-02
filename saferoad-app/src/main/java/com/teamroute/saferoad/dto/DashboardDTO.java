package com.teamroute.saferoad.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardDTO {
    // 상단 KPI
    private long todayCount;       // 오늘 탐지된 수
    private long unprocessedCount; // 미확인(미처리) 수

    // 위험 장애물 Top 10
    private List<DangerItem> topRiskGroups;

    // 일별 차트 데이터 (최근 30일 수치 배열)
    private List<Long> dailyCounts;

    @Getter
    @Builder
    public static class DangerItem {
        private Long groupId;
        private String title;    // 예: 포트홀 (부산진구 중앙대로)
        private int score;       // 위험도 점수 (0~100)
        private String location;
        private String state;    // 처리 상태
    }
}