package com.booktalk.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 네이티브 카카오 SDK 로그인 요청. 앱에서 받은 카카오 access token을 전달한다.
 */
public record KakaoTokenLoginRequest(
        @NotBlank(message = "accessToken은 필수입니다.") String accessToken
) {
}
