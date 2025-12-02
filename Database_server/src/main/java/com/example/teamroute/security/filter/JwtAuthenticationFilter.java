package com.example.teamroute.security.filter;
import com.example.teamroute.security.controller.JwtTokenProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import java.io.IOException;

// API 요청을 가로채서 토큰을 검사하는 필터
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 1. 헤더에서 토큰 꺼내기
        String token = resolveToken((HttpServletRequest) request);

        // 2. 토큰이 있고, 유효하다면
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // (실제로는 여기서 SecurityContext에 인증 정보를 넣음)
            System.out.println("인증 성공: 유효한 토큰입니다.");
        }

        chain.doFilter(request, response);
    }

    // Bearer 떼고 토큰만 가져오기
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}