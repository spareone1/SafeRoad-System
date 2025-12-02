package com.teamroute.saferoad.api;

import com.teamroute.saferoad.domain.User;
import com.teamroute.saferoad.dto.LoginRequestDTO;
import com.teamroute.saferoad.dto.UserRequestDTO;
import com.teamroute.saferoad.security.JwtTokenProvider;
import com.teamroute.saferoad.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApi {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    // application.properties에서 토큰 만료 시간(초)
    @Value("${app.jwt.access-seconds}")
    private long accessSeconds;

    @Value("${app.jwt.refresh-seconds}")
    private long refreshSeconds;

    /**
     * API 회원가입
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequestDTO req) {
        try {
            userService.register(req);
            return ResponseEntity.ok(Map.of("ok", true));

        } catch (IllegalArgumentException e) {
            String errorCode = "BAD_REQUEST";
            if (e.getMessage().contains("아이디")) {
                errorCode = "USERID_TAKEN";
            }
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", errorCode, "message", e.getMessage()));
        }
    }

    /**
     * API 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDTO dto,
            HttpServletResponse response
    ) {
        try {
            // UserService에서 로그인 검증
            User user = userService.login(dto);

            // UserDetails 조회 (권한 정보 포함)
            UserDetails userDetails = userService.loadUserByUsername(user.getUserid());
            String userid = userDetails.getUsername();

            // 권한(Role) 문자열 추출
            String authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            // JWT 클레임 생성
            Map<String, Object> claims = Map.of(
                    "name", user.getName(),
                    "role", authorities
            );

            // 토큰 생성
            String accessToken = tokenProvider.createAccessToken(userid, claims);
            String refreshToken = tokenProvider.createRefreshToken(userid, Map.of());

            // AccessToken을 HttpOnly 쿠키로 설정
            Cookie accessTokenCookie = createCookie("accessToken", accessToken, (int) accessSeconds);
            response.addCookie(accessTokenCookie);

            // RefreshToken을 HttpOnly 쿠키로 설정
            Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken, (int) refreshSeconds);
            response.addCookie(refreshTokenCookie);

            // 본문(body)으로는 토큰을 제외한 정보만 반환
            return ResponseEntity.ok(Map.of(
                    "name", user.getName(),
                    "role", authorities
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "BAD_CREDENTIALS", "message", e.getMessage()));
        }
    }

    /**
     * 로그아웃 API
     * (토큰 쿠키 삭제)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        response.addCookie(createCookie("accessToken", null, 0));
        response.addCookie(createCookie("refreshToken", null, 0));
        return ResponseEntity.ok(Map.of("ok", true, "message", "Logged out successfully"));
    }

    /**
     * HttpOnly 쿠키 생성 헬퍼
     */
    private Cookie createCookie(String name, String value, int maxAgeInSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeInSeconds);
        return cookie;
    }

    /**
     * 아이디 중복 확인 API
     */
    @GetMapping("/check-userid")
    public ResponseEntity<?> checkUseridDuplicate(@RequestParam("userid") String userid) {
        if (userid == null || userid.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("isAvailable", false, "message", "아이디를 입력해주세요."));
        }
        boolean isAvailable = !userService.checkUseridDuplicate(userid);
        return ResponseEntity.ok(Map.of("isAvailable", isAvailable));
    }


    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("ok", false));
        var uOpt = userService.findByUserid(auth.getName());
        if (uOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("ok", false));
        var u = uOpt.get();
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "userid", u.getUserid(),
                "name", u.getName(),
                "status", u.getUserstatus().name()
        ));
    }
}