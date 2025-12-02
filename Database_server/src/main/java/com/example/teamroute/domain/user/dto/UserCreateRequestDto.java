package com.example.teamroute.domain.user.dto;

import com.example.teamroute.domain.user.entity.User;
import com.example.teamroute.domain.user.entity.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateRequestDto { // 회원가입용 Dto
    // 클라이언트가 보내주는 필드명 (camelCase 추천)
    private String userId;
    private String pw;
    private String name;

    // DTO -> Entity 변환 메서드 (빌더 패턴 사용!)
    public User toEntity() {
        return User.builder()
                .userid(this.userId)       // 엔티티 필드명: userid
                .pw(this.pw)
                .name(this.name)
                .userstatus(UserStatus.normal_user) // 가입 시 기본값 설정
                .created_at(LocalDateTime.now())    // 생성 시간 필수!
                .updated_at(LocalDateTime.now())    // 수정 시간 필수!
                .build();
    }
}