package com.booktalk.domain.user.dto;

import com.booktalk.domain.user.User;

/**
 * 로그인 사용자 정보 공통 응답.
 * /api/v1/auth/me, /api/v1/auth/{provider}/login, /api/v1/users/me 에서 동일하게 사용한다.
 */
public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String profileColor,
        String oauthProvider,
        boolean onboardingCompleted
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getProfileColor(),
                user.getOauthProvider(),
                user.isOnboardingCompleted()
        );
    }
}
