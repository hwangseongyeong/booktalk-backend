package com.booktalk.domain.admin.dto;

import com.booktalk.domain.user.User;

import java.time.LocalDateTime;

/** 어드민 회원 목록 한 줄. */
public record AdminUserListItem(
        Long id,
        String nickname,
        String email,
        String profileImageUrl,
        String oauthProvider,
        String role,
        boolean suspended,
        LocalDateTime createdAt
) {
    public static AdminUserListItem from(User u) {
        return new AdminUserListItem(
                u.getId(), u.getNickname(), u.getEmail(), u.getProfileImageUrl(),
                u.getOauthProvider(), u.getRole().name(), u.isSuspended(), u.getCreatedAt());
    }
}
