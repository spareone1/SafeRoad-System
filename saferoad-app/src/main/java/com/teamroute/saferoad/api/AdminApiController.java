package com.teamroute.saferoad.api;

import com.teamroute.saferoad.dto.DashboardDTO;
import com.teamroute.saferoad.service.ObstacleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {

    private final ObstacleService obstacleService;

    /**
     * 대시보드 데이터 조회 API
     * (KPI, Top 10 위험 지역, 일별 차트 데이터)
     * GET /api/admin/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboardData() {
        DashboardDTO data = obstacleService.getDashboardData();
        return ResponseEntity.ok(data);
    }
}