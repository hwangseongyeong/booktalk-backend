package com.booktalk.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * MVP 단계 임시 설정.
 *
 * spring-security 의존성이 클래스패스에 있으면 기본적으로 모든 요청에 로그인을 요구하므로,
 * 실제 JWT/OAuth2 인증이 붙기 전까지는 전체 API를 개방해둔다.
 * 이 기간 동안 "현재 사용자"는 {@link com.booktalk.domain.user.DemoUserProvider} 가 담당한다.
 *
 * TODO: JWT 인증 필터 추가 후 anyRequest().permitAll() 을 실제 인가 규칙으로 교체할 것.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
