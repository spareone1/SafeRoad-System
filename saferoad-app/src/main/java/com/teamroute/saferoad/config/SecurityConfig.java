package com.teamroute.saferoad.config;

import com.teamroute.saferoad.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectProvider<ClientRegistrationRepository> clientRegRepoProvider;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 권한 매핑
                .authorizeHttpRequests(auth -> auth
                        // --- 모두 허용 (permitAll) ---
                        .requestMatchers(
                                "/", "/index", "/info",
                                "/register-done",
                                "/withdraw-success"
                        ).permitAll()

                        // --- 익명 사용자만 허용 (anonymous) ---
                        .requestMatchers("/login", "/register").anonymous()

                        // --- 정적 리소스 허용 (permitAll) ---
                        .requestMatchers(
                                "/favicon.ico", "/css/**", "/js/**", "/img/**", "/assets/**", "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/oauth2/**").permitAll()

                        // --- 인증 없이 허용할 API (permitAll) ---
                        // (회원가입, 중복확인, 로그인만 허용)
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/check-userid",
                                "/api/auth/login"
                        ).permitAll()

                        // --- 관리자 전용 (authenticated) ---
                        .requestMatchers("/admin/**", "/dashboard/**", "/api/admin/**").hasRole("ADMIN")

                        // --- 로그인 사용자 전용 (authenticated) ---
                        // (페이지)
                        .requestMatchers(
                                "/check-map/**", "/check-list/**", "/mypage/**", "/profile/**", "/user/**",
                                "/withdraw",
                                "/withdraw/proceed"
                        ).authenticated()

                        //  검색 및 장애물 관련 API는 이제 인증된 사용자만 접근 가능
                        .requestMatchers(
                                "/api/search/**",
                                "/api/obstacle-group/**", // 마커, 상세 조회, 상태 변경 등
                                "/api/**" // 그 외 모든 API
                        ).authenticated()

                        // 나머지는 기본 인증
                        .anyRequest().authenticated()
                )

                // 401/403 응답 명확화
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            boolean isApiRequest = request.getRequestURI().startsWith("/api/");
                            if (isApiRequest) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            boolean isApiRequest = request.getRequestURI().startsWith("/api/");
                            if (isApiRequest) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        ClientRegistrationRepository cr = clientRegRepoProvider.getIfAvailable();
        if (cr != null) {
            http.oauth2Login(oauth -> oauth.loginPage("/login"));
        }

        return http.build();
    }
}