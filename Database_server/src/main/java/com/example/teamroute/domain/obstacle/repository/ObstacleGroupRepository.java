package com.example.teamroute.domain.obstacle.repository;

// ObstacleGroupRepository.java (인터페이스)

import com.example.teamroute.domain.obstacle.entity.ObstacleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ObstacleGroupRepository extends JpaRepository<ObstacleGroup, Long> {

    @Query("SELECT og FROM ObstacleGroup og JOIN og.obstacles o " +
            "WHERE o.created_at >= :startTime " +  // ★ 필드 이름이 created_at 이므로 그대로 유지 (문제 없음)
            "GROUP BY og.id")
    List<ObstacleGroup> findRecentlyActiveGroups(@Param("startTime") LocalDateTime searchStartTime);
}