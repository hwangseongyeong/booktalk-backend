package com.booktalk.global.security;

import com.booktalk.domain.user.User;
import com.booktalk.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * JwtAuthenticationFilter가 SecurityContext에 채워둔 사용자 ID로 현재 로그인한 User를 조회한다.
 * Phase 1의 DemoUserProvider를 대체한다.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new InvalidTokenException("존재하지 않는 사용자입니다."));
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new InvalidTokenException("로그인이 필요합니다.");
        }
        return userId;
    }
}
