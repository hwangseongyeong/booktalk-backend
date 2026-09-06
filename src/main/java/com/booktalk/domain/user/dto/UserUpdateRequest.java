package com.booktalk.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 프로필 수정(온보딩 닉네임 설정 포함) 요청.
 * profileImageUrl / profileColor 는 선택이며, 전달된 값만 반영된다.
 */
public record UserUpdateRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 12, message = "닉네임은 2~12자로 입력해주세요.")
        String nickname,

        String profileImageUrl,

        @Pattern(regexp = "^#([0-9a-fA-F]{6})$", message = "프로필 색은 #RRGGBB 형식이어야 합니다.")
        String profileColor
) {
}
