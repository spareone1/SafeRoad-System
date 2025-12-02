package com.example.teamroute.security.controller;

import com.example.teamroute.security.dto.LoginRequestDto;
import com.example.teamroute.security.dto.LoginResponseDto;
import com.example.teamroute.domain.user.repository.UserRepository;
import com.example.teamroute.domain.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.teamroute.security.controller.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {


        // 1. DB에서 유저 찾기 (userid로 조회)
        User user = userRepository.findByUserid(request.getUserid())
                .orElse(null);

        // 2. 유저가 없거나 비밀번호가 틀리면 에러 리턴
        // (비밀번호 암호화 안 썼으면 user.getPassword().equals(request.getPassword()) 사용)
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPw())) {
            return ResponseEntity.status(401).body("로그인 실패: 아이디나 비번 틀림");
        }

        // 3. 로그인 성공 - 토큰 생성 (유저 ID와 권한 등을 넣음)
        String token = jwtTokenProvider.createToken(user.getUserid(), user.getId());

        // 4. 파이썬한테 토큰과 userId(PK)를 줌
        return ResponseEntity.ok(new LoginResponseDto(token, user.getId(), "Login Success"));
    }
}