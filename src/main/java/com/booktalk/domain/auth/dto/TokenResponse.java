package com.booktalk.domain.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
