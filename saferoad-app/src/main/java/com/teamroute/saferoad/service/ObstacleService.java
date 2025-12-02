package com.teamroute.saferoad.service;

import com.teamroute.saferoad.domain.Obstacle;
import com.teamroute.saferoad.domain.ObstacleGroup;
import com.teamroute.saferoad.dto.DashboardDTO;
import com.teamroute.saferoad.dto.MapMarkerDTO;
import com.teamroute.saferoad.dto.MyReportDTO;
import com.teamroute.saferoad.dto.UpdateStateDTO;
import com.teamroute.saferoad.repository.ObstacleGroupRepository;
import com.teamroute.saferoad.repository.ObstacleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ObstacleService {

    private final ObstacleGroupRepository obstacleGroupRepository;
    private final ObstacleRepository obstacleRepository;

    public Page<ObstacleGroup> findAllObstacleGroups(Pageable pageable) {
        return obstacleGroupRepository.findAllWithObstacles(pageable);
    }

    public Page<ObstacleGroup> findAllObstacleGroups(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return obstacleGroupRepository.searchByKeyword(keyword, pageable);
        }
        return obstacleGroupRepository.findAllWithObstacles(pageable);
    }

    public Optional<ObstacleGroup> findGroupDetailsById(Long id) {
        return obstacleGroupRepository.findByIdWithObstacles(id);
    }

    @Transactional
    public ObstacleGroup updateGroupState(Long id, UpdateStateDTO dto) {
        ObstacleGroup group = obstacleGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID " + id + "에 해당하는 장애물 그룹을 찾을 수 없습니다."));
        try {
            ObstacleGroup.State newState = ObstacleGroup.State.valueOf(dto.getState());
            group.setState(newState);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 상태 값입니다: " + dto.getState());
        }
        return group;
    }

    /**
     *  장애물 그룹 삭제 (신고 반려)
     */
    @Transactional
    public void deleteGroup(Long id) {
        ObstacleGroup group = obstacleGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID " + id + "에 해당하는 장애물 그룹을 찾을 수 없습니다."));

        // DB의 ON DELETE CASCADE 설정에 의해 하위 Obstacle도 함께 삭제
        // JPA 연관관계 설정(CascadeType.REMOVE)이 되어 있다면 JPA가 처리
        obstacleGroupRepository.delete(group);
    }

    public List<MapMarkerDTO> findMarkersWithinBounds(double minLat, double maxLat, double minLon, double maxLon) {
        return obstacleGroupRepository.findMarkersWithinBounds(minLat, maxLat, minLon, maxLon);
    }

    public List<MyReportDTO> getMyRecentReports(String userid) {
        Pageable limit = PageRequest.of(0, 5);
        List<Obstacle> obstacles = obstacleRepository.findMyReports(userid, limit);
        return obstacles.stream().map(MyReportDTO::new).collect(Collectors.toList());
    }

    // --- 대시보드 로직 ---

    /**
     * 대시보드 전체 데이터 조회
     * (KPI, Top 10 위험 지역, 일별 차트 데이터)
     */
    public DashboardDTO getDashboardData() {
        // KPI: 오늘 탐지된 수
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long todayCount = obstacleRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        // KPI: 미확인(non_processed) 그룹 수
        long unprocessedCount = obstacleGroupRepository.countByState(ObstacleGroup.State.non_processed);

        // 차트: 최근 30일 일별 탐지 건수
        List<Long> dailyCounts = getDailyCountsLast30Days();

        // 랭킹: 위험도 Top 10 계산
        List<DashboardDTO.DangerItem> topRiskGroups = calculateTopRiskGroups();

        return DashboardDTO.builder()
                .todayCount(todayCount)
                .unprocessedCount(unprocessedCount)
                .dailyCounts(dailyCounts)
                .topRiskGroups(topRiskGroups)
                .build();
    }

    /**
     * 위험도 Top 10 계산 로직
     */
    private List<DashboardDTO.DangerItem> calculateTopRiskGroups() {
        // 처리 완료된 건을 제외하고 모두 가져옴 (N+1 방지된 쿼리 사용)
        List<ObstacleGroup> groups = obstacleGroupRepository.findAllActiveWithObstacles();

        // 점수 계산 및 DTO 변환
        return groups.stream()
                .map(group -> {
                    int score = calculateRiskScore(group);
                    // 대표 장애물 이름 (없으면 '알 수 없음')
                    String title = group.getObstacles().isEmpty() ?
                            "알 수 없는 장애물" : group.getObstacles().get(0).getObstacleName();

                    return DashboardDTO.DangerItem.builder()
                            .groupId(group.getId())
                            .title(title + " (" + group.getLocation() + ")")
                            .location(group.getLocation())
                            .state(group.getState().name())
                            .score(score)
                            .build();
                })
                .sorted(Comparator.comparingInt(DashboardDTO.DangerItem::getScore).reversed()) // 점수 높은 순
                .limit(10) // 상위 10개
                .collect(Collectors.toList());
    }

    /**
     * 위험도 점수 계산 알고리즘
     */
    private int calculateRiskScore(ObstacleGroup group) {
        List<Obstacle> obstacles = group.getObstacles();
        if (obstacles == null || obstacles.isEmpty()) return 0;

        int count = obstacles.size();

        // 시간 밀집도 계산 (첫 신고 ~ 마지막 신고 차이)
        LocalDateTime minTime = obstacles.stream().map(Obstacle::getCreated_at).min(LocalDateTime::compareTo).orElse(LocalDateTime.now());
        LocalDateTime maxTime = obstacles.stream().map(Obstacle::getCreated_at).max(LocalDateTime::compareTo).orElse(LocalDateTime.now());

        long hoursDiff = ChronoUnit.HOURS.between(minTime, maxTime);

        // 가중치 적용
        double multiplier = 1.0;
        if (hoursDiff < 1) multiplier = 2.0;       // 1시간 이내 급증: 매우 위험
        else if (hoursDiff < 6) multiplier = 1.5;  // 6시간 이내: 위험
        else if (hoursDiff < 24) multiplier = 1.2; // 하루 이내: 주의

        // 점수 산출 (기본 10점 * 개수 * 가중치)
        int score = (int) ((count * 10) * multiplier);

        // 100점 만점 제한
        return Math.min(100, score);
    }

    /**
     * 최근 30일 일별 통계 데이터 생성 (빈 날짜는 0으로 채움)
     */
    private List<Long> getDailyCountsLast30Days() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29); // 오늘 포함 30일

        // DB에서 데이터 조회 (날짜별 그룹핑된 결과)
        List<Object[]> rawData = obstacleRepository.findDailyCounts(startDate.toString());

        // Map으로 변환 (Key: 날짜String, Value: Count)
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] row : rawData) {
            // MySQL DATE() 결과는 java.sql.Date 또는 String일 수 있음
            String dateStr = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            countMap.put(dateStr, count);
        }

        // 30일치 리스트 생성 (비어있는 날짜는 0으로 채움)
        List<Long> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < 30; i++) {
            String key = startDate.plusDays(i).format(formatter);
            result.add(countMap.getOrDefault(key, 0L));
        }

        return result;
    }
}