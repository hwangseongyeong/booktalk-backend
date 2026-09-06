package com.booktalk.domain.auth.dto;

import com.booktalk.domain.user.dto.UserProfileResponse;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        UserProfileResponse user
) {
}
