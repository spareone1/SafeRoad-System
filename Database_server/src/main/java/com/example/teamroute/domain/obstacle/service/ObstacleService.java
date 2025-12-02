package com.example.teamroute.domain.obstacle.service;
import com.example.teamroute.domain.obstacle.entity.ObstacleState;
import com.example.teamroute.domain.obstacle.dto.ObstacleRequestDto;
import com.example.teamroute.domain.obstacle.dto.ObstacleResponseDto;
import com.example.teamroute.domain.obstacle.entity.Obstacle;
import com.example.teamroute.domain.obstacle.entity.ObstacleGroup;
import com.example.teamroute.domain.obstacle.repository.ObstacleGroupRepository;
import com.example.teamroute.domain.obstacle.repository.ObstacleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.example.teamroute.domain.user.entity.User;
import com.example.teamroute.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ObstacleService {

    private final GeocodingService geocodingService;
    private final ObstacleRepository obstacleRepository;
    private final UserRepository userRepository;
    private final ObstacleGroupRepository obstacleGroupRepository;

    // Haversine 공식 구현 (내부 도우미 메서드)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // 결과: 미터 (m)
    }

    // 핵심 로직: 장애물 그룹 찾기/생성
    private ObstacleGroup findOrCreateGroup(double lat, double lon) {
        // 1. 임계값 설정 (예: 10m 이내, 5분 이내)
        final double DISTANCE_THRESHOLD_M = 10.0;
        final int TIME_THRESHOLD_MINUTES = 5;

        // 2. 검색 시작 시간 설정 (현재 시간 - 5분)
        LocalDateTime searchStartTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(TIME_THRESHOLD_MINUTES);

        // 3. 거리/시간 조건을 만족하는 기존 그룹을 DB에서 조회
        List<ObstacleGroup> potentialGroups =
                obstacleGroupRepository.findRecentlyActiveGroups(searchStartTime);

        // 4. 조회된 그룹들을 Haversine 공식을 사용하여 검토
        for (ObstacleGroup group : potentialGroups) {
            // 그룹의 가장 최근 장애물 위치를 그룹 대표 위치로 간주
            Obstacle latestObstacle = group.getObstacles().stream()
                    .max((o1, o2) -> o1.getCreated_at().compareTo(o2.getCreated_at()))
                    .orElse(null);

            if (latestObstacle != null) {
                double distance = calculateDistance(
                        lat, lon, latestObstacle.getLat(), latestObstacle.getLon()
                );

                if (distance <= DISTANCE_THRESHOLD_M) {
                    return group; // 중복 발견! 기존 그룹 반환
                }
            }
        }

        // 5. 중복이 없다면, 새 그룹 생성 및 반환
        String actualAddress = geocodingService.getAddressFromCoords(lat, lon);
        return obstacleGroupRepository.save(
                ObstacleGroup.builder()
                        .location(actualAddress) // ★ 실제 주소 저장
                        .state(ObstacleState.non_processed)
                        .build()
        );
    }

    // saveObstacle 메서드 수정
    public ObstacleResponseDto saveObstacle(ObstacleRequestDto dto) {
        // 1) 유저 엔티티 조회
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다. id=" + dto.getUserId()));

        // 2) 중복 확인 및 그룹 찾기/생성 // ✅ 핵심 로직 호출
        ObstacleGroup obstacleGroup = findOrCreateGroup(dto.getLat().doubleValue(), dto.getLon().doubleValue());
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusHours(9);
        // 3) Obstacle 생성 및 그룹에 연결
        Obstacle obstacle = Obstacle.builder()
                .user(user)
                .obstacleGroup(obstacleGroup) // ✅ 그룹 연결
                .obstacleName(dto.getObstacleName())
                .lat(dto.getLat().floatValue())
                .lon(dto.getLon().floatValue())
                .imageloc(dto.getImage())
                .created_at(now)
                .updated_at(now)
                .build();

        obstacleRepository.save(obstacle);

        return new ObstacleResponseDto("ok", "장애물 정보 수신 및 그룹 처리 완료", obstacle);
    }
    public List<Obstacle> getAllObstacles() {
        return obstacleRepository.findAll();
    }
}

