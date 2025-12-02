package com.example.teamroute.domain.obstacle.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "obstaclegroup")
@Getter @Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor @AllArgsConstructor @Builder
public class ObstacleGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObstacleState state;

    @JsonIgnore
    @OneToMany(mappedBy = "obstacleGroup", cascade = CascadeType.ALL)
    private List<Obstacle> obstacles;
}




