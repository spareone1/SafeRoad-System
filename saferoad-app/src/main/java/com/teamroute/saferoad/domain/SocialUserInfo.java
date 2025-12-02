package com.teamroute.saferoad.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="Socialuserinfo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SocialUserInfo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String puserid;

    @Column(nullable = false)
    private String email;
}
