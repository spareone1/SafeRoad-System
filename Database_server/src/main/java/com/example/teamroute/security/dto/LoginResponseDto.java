package com.example.teamroute.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String token;   // 파이썬이 "token"을 기다림
    private Long userId;    // 파이썬이 "userId"를 기다림
    private String message;
}