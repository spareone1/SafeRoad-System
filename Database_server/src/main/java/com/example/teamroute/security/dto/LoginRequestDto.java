package com.example.teamroute.security.dto;

import lombok.Data;
import lombok.ToString;

@Data
public class LoginRequestDto {
    private String userid;   // 파이썬에서 "userid"로 보냄 (소문자 주의)
    @ToString.Exclude
    private String password; // 파이썬에서 "password"로 보냄
}