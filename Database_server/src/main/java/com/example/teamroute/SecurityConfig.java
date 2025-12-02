 package com.example.teamroute;

import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

// @Configuration
// public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())        // CSRF 비활성화
//                 .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll()     // 모든 요청 허용
//                );
//        return http.build();
//    }
//}

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (일반적인 API 서버에서 사용)
                .csrf(csrf -> csrf.disable())

                // 세션 관리를 Stateless로 설정 (토큰 방식)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 요청 권한 설정 (★★ 모든 요청 허용으로 변경! ★★)
                .authorizeHttpRequests(auth -> auth
                        // 모든 요청(anyRequest)에 대해 인증 없이 접근 허용(permitAll)
                        .anyRequest().permitAll()
                );

        return http.build();
    }
    // ... 기타 설정
}
