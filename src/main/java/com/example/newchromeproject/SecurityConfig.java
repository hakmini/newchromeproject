package com.example.newchromeproject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // ✅ WebSocket 연결 시 CSRF 토큰 불필요
                .authorizeHttpRequests(auth -> auth
                        // ✅ WebSocket (React) 연결 허용
                        .requestMatchers("/coinprice/**", "/topic/**", "/app/**").permitAll()
                        // ✅ REST 테스트용 endpoint도 허용 가능
                        .requestMatchers("/api/**").permitAll()
                        // ✅ 나머지 URL은 인증 필요 (원한다면 .permitAll()로 완전 오픈)
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable()) // ✅ 기본 로그인 페이지 비활성화
                .httpBasic(basic -> basic.disable()); // ✅ REST BasicAuth도 비활성화

        return http.build();
    }
}