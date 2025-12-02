package com.teamroute.saferoad.repository;

import com.teamroute.saferoad.domain.ObstacleGroup;
import com.teamroute.saferoad.dto.MapMarkerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ObstacleGroupRepository extends JpaRepository<ObstacleGroup, Long> {

    @Query(value = "SELECT DISTINCT og FROM ObstacleGroup og LEFT JOIN FETCH og.obstacles",
            countQuery = "SELECT COUNT(og) FROM ObstacleGroup og")
    Page<ObstacleGroup> findAllWithObstacles(Pageable pageable);

    @Query(value = "SELECT DISTINCT og FROM ObstacleGroup og " +
            "LEFT JOIN FETCH og.obstacles o " +
            "WHERE og.location LIKE %:keyword% OR o.obstacleName LIKE %:keyword%",
            countQuery = "SELECT COUNT(DISTINCT og) FROM ObstacleGroup og " +
                    "LEFT JOIN og.obstacles o " +
                    "WHERE og.location LIKE %:keyword% OR o.obstacleName LIKE %:keyword%")
    Page<ObstacleGroup> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT og FROM ObstacleGroup og LEFT JOIN FETCH og.obstacles WHERE og.id = :id")
    Optional<ObstacleGroup> findByIdWithObstacles(@Param("id") Long id);

    @Query("SELECT new com.teamroute.saferoad.dto.MapMarkerDTO(" +
            "og.id, o.obstacleName, o.lat, o.lon, og.state) " +
            "FROM ObstacleGroup og " +
            "JOIN og.obstacles o " +
            "WHERE o.lat BETWEEN :minLat AND :maxLat " +
            "AND o.lon BETWEEN :minLon AND :maxLon")
    List<MapMarkerDTO> findMarkersWithinBounds(@Param("minLat") double minLat,
                                               @Param("maxLat") double maxLat,
                                               @Param("minLon") double minLon,
                                               @Param("maxLon") double maxLon);

    /**
     * 상태별 그룹 개수 조회 (KPI: 미확인 장애물 수)
     */
    long countByState(ObstacleGroup.State state);

    /**
     * 위험도 분석용 전체 조회
     * '처리 완료'가 아닌 모든 그룹을 Obstacle 목록과 함께 조회.
     * (Service에서 알고리즘으로 점수를 계산하여 Top 10 추출.)
     */
    @Query("SELECT DISTINCT og FROM ObstacleGroup og LEFT JOIN FETCH og.obstacles WHERE og.state != 'completed'")
    List<ObstacleGroup> findAllActiveWithObstacles();
}