package com.teamroute.saferoad.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long accessMillis;
    private final long refreshMillis;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-seconds}") long accessSeconds,
            @Value("${app.jwt.refresh-seconds}") long refreshSeconds
    ) {
        byte[] k = decodeSecret(secret);
        this.key = Keys.hmacShaKeyFor(k);
        this.accessMillis = accessSeconds * 1000L;
        this.refreshMillis = refreshSeconds * 1000L;
    }

    private byte[] decodeSecret(String s) {
        try {
            return java.util.Base64.getDecoder().decode(s);
        } catch (IllegalArgumentException e) {
            return s.getBytes(StandardCharsets.UTF_8);
        }
    }

    public String createAccessToken(String sub, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(sub)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String sub, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(sub)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
