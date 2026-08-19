package com.booktalk.domain.auth;

/** 각 제공자의 서로 다른 응답 포맷을 표준화한 형태. */
public record OAuthUserInfo(
        OAuthProvider provider,
        String providerId,
        String email,
        String nickname,
        String profileImageUrl
) {
}
