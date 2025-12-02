package com.teamroute.saferoad.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name="User")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userid;

    @Column(nullable = false)
    private String pw; // BCrypt hash

    private String name;

    @Column(nullable = false)
    private LocalDateTime created_at;

    @Column(nullable = false)
    private LocalDateTime updated_at;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userstatus;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        created_at = now;
        updated_at = now;
        if (userstatus == null) userstatus = UserStatus.normal_user;
    }

    @PreUpdate
    public void onUpdate() {
        updated_at = LocalDateTime.now();
    }

    public enum UserStatus {
        deleted, normal_user, admin_user
    }
}
