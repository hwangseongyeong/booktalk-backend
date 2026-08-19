package com.booktalk.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실제 JWT/OAuth2 로그인이 붙기 전까지 사용하는 임시 "현재 사용자" 해석기.
 * 모든 요청을 단일 데모 사용자 기준으로 처리한다.
 *
 * 인증이 도입되면 이 클래스를 SecurityContext 기반의
 * CurrentUserResolver 로 교체하고, 이 클래스를 사용하는 서비스들의
 * demoUserProvider.getOrCreateDemoUser() 호출부만 바꿔주면 된다.
 */
@Component
@RequiredArgsConstructor
public class DemoUserProvider {

    private static final String DEMO_EMAIL = "demo@booktalk.local";

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateDemoUser() {
        return userRepository.findByEmail(DEMO_EMAIL)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(DEMO_EMAIL)
                        .nickname("데모 사용자")
                        .oauthProvider("DEMO")
                        .build()));
    }
}
