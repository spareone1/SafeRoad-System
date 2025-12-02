package com.teamroute.saferoad.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// 실제 DB 테이블 이름(obstaclegroup)으로 변경
@Entity @Table(name="obstaclegroup")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ObstacleGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String location;

    public enum State { processing, completed, non_processed }

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private State state = State.non_processed;

    @OneToMany(mappedBy = "obstaclegroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Obstacle> obstacles = new ArrayList<>();
}