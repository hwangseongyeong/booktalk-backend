package com.booktalk.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
        @NotBlank(message = "code는 필수입니다.") String code,
        String redirectUri, // 카카오/구글/페이스북에서 사용
        String state // 네이버에서 사용
) {
}
