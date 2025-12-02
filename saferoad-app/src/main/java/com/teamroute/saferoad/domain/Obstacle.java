package com.teamroute.saferoad.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// 실제 DB 테이블 이름(obstacle)로 명시
@Entity @Table(name="obstacle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Obstacle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="obstaclegroup_id")
    private ObstacleGroup obstaclegroup; // 필드명은 obstaclegroup (mappedBy와 일치)

    private String obstacleName;

    @Column(nullable=false)
    private float lat;

    @Column(nullable=false)
    private float lon;

    @Column(nullable=false)
    private LocalDateTime created_at;

    @Column(nullable=false)
    private LocalDateTime updated_at;

    @Column(nullable=false)
    private String imageloc;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        created_at = now;
        updated_at = now;
    }

    @PreUpdate
    public void onUpdate() { updated_at = LocalDateTime.now(); }
}