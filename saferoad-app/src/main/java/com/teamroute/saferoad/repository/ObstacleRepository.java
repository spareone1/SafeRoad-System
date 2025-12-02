package com.teamroute.saferoad.repository;

import com.teamroute.saferoad.domain.Obstacle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ObstacleRepository extends JpaRepository<Obstacle, Long> {

    /**
     * 내 신고 내역 조회 (최신순)
     */
    @Query("SELECT o FROM Obstacle o " +
            "LEFT JOIN FETCH o.obstaclegroup " +
            "WHERE o.user.userid = :userid " +
            "ORDER BY o.created_at DESC")
    List<Obstacle> findMyReports(@Param("userid") String userid, Pageable pageable);

    /**
     * 특정 기간 동안 생성된 장애물 개수 조회 (KPI: 오늘 탐지된 수)
     */
    @Query("SELECT COUNT(o) FROM Obstacle o WHERE o.created_at BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 최근 30일간 일별 탐지 건수 조회 (차트용)
     * MySQL의 DATE() 함수를 사용하기 위해 nativeQuery = true로 설정
     * 결과는 Object[] { 날짜(String/Date), 개수(Number) } 형태의 리스트로 반환
     */
    @Query(value = "SELECT DATE(created_at) as d, COUNT(*) as c " +
            "FROM obstacle " +
            "WHERE created_at >= :startDate " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY d ASC", nativeQuery = true)
    List<Object[]> findDailyCounts(@Param("startDate") String startDate);
}