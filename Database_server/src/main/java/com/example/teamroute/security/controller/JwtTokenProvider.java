package com.example.teamroute.security.controller;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // 비밀키 (실무에선 application.yml에 숨겨야 함. 일단은 하드코딩)
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long VALIDITY_IN_MS = 3600000; // 1시간 유효

    public String createToken(String userid, Long id) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + VALIDITY_IN_MS);

        return Jwts.builder()
                .setSubject(userid) // 토큰 제목(유저 아이디)
                .claim("id", id)    // 토큰 안에 담을 정보 (PK)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key)
                .compact();
    }

    // 2. 토큰 유효성 검증 (API 호출 시 필터에서 호출) ✅ 추가된 부분
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 유효함
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 위조되었거나 만료됨
        }
    }

    // 3. 토큰에서 사용자 ID 추출 ✅ 추가된 부분
    public String getUserId(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
}