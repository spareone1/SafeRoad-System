package com.example.teamroute.domain.user.dto;

import com.example.teamroute.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto {
    private Long id;
    private String userId;      // JSON으로 나갈 때는 camelCase가 국룰
    private String name;
    private String userStatus;  // 문자열로 변환된 상태값
    private LocalDateTime createdAt;

    // Entity -> DTO 변환 생성자
    public UserResponseDto(User user) {
        this.id = user.getId();
        this.userId = user.getUserid(); // 엔티티의 userid 꺼내오기
        this.name = user.getName();
        // Enum -> String 변환 (null 체크 포함)
        this.userStatus = user.getUserstatus() != null ? user.getUserstatus().name() : null;
        this.createdAt = user.getCreated_at(); // 필드명 created_at 주의
    }
}