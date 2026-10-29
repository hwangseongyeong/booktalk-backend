package com.booktalk.domain.admin.dto;

import com.booktalk.domain.user.User;

import java.time.LocalDateTime;

/** 어드민 회원 상세. 프로필 + 활동 수. */
public record AdminUserDetail(
        Long id,
        String nickname,
        String email,
        String profileImageUrl,
        String profileColor,
        String oauthProvider,
        String role,
        boolean suspended,
        boolean onboardingCompleted,
        LocalDateTime createdAt,
        long readingRecordCount,
        long bookNoteCount
) {
    public static AdminUserDetail of(User u, long readingRecordCount, long bookNoteCount) {
        return new AdminUserDetail(
                u.getId(), u.getNickname(), u.getEmail(), u.getProfileImageUrl(), u.getProfileColor(),
                u.getOauthProvider(), u.getRole().name(), u.isSuspended(), u.isOnboardingCompleted(),
                u.getCreatedAt(), readingRecordCount, bookNoteCount);
    }
}
