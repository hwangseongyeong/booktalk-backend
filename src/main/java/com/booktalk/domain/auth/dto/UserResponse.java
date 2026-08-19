package com.booktalk.domain.auth.dto;

import com.booktalk.domain.user.User;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String oauthProvider
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getOauthProvider()
        );
    }
}
