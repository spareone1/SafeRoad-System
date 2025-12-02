package com.example.teamroute.domain.user.entity;

import com.example.teamroute.domain.obstacle.entity.Obstacle;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userid;

    @Column(nullable = false)
    private String pw;

    private String name;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updated_at;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userstatus;

    // 연관관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Obstacle> obstacles;
}
