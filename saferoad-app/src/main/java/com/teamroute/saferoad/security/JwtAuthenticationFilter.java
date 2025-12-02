package com.teamroute.saferoad.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 이미 인증이 있으면 패스
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 쿠키를 읽도록 수정된 resolveToken 호출
        String token = resolveToken(request);

        if (token != null && !token.isBlank()) {
            try {
                Claims claims = tokenProvider.parse(token).getBody();
                String userid = claims.getSubject();

                // role/roles 클레임 모두 지원
                Collection<? extends GrantedAuthority> authorities = mapAuthorities(
                        claims.get("role"),
                        claims.get("roles")
                );

                if (userid != null) {
                    var authToken = new UsernamePasswordAuthenticationToken(userid, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // 토큰 오류 시 인증 컨텍스트를 건드리지 않고 통과(=익명)
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 3. [수정] 헤더 또는 쿠키에서 토큰을 추출합니다.
     */
    private String resolveToken(HttpServletRequest request) {
        // 1. (기존) Authorization 헤더에서 "Bearer" 토큰 확인
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. (신규) 헤더에 없으면 'accessToken' 쿠키에서 확인
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null; // 헤더와 쿠키 모두에 토큰이 없음
    }

    /**
     * role(문자열) 또는 roles(배열)에서 ROLE 매핑 생성
     */
    private Collection<? extends GrantedAuthority> mapAuthorities(Object roleClaim, Object rolesClaim) {
        Set<String> raw = new HashSet<>();

        if (roleClaim instanceof String s) {
            raw.add(s);
        }
        if (rolesClaim instanceof Collection<?> c) {
            raw.addAll(c.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.toSet()));
        }

        // 기본값: 사용자
        if (raw.isEmpty()) raw.add("normal_user");

        boolean isAdmin = raw.stream().anyMatch(this::isAdminRole);

        String mapped = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        return List.of(new SimpleGrantedAuthority(mapped));
    }

    private boolean isAdminRole(String r) {
        if (r == null) return false;
        String s = r.trim().toUpperCase(Locale.ROOT);
        // admin_user / ADMIN / ROLE_ADMIN 등 넓게 허용
        return s.equals("ROLE_ADMIN") || s.equals("ADMIN") || s.equals("ADMIN_USER");
    }
}