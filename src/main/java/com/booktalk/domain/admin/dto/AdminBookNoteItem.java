package com.booktalk.domain.admin.dto;

import com.booktalk.domain.booknote.BookNote;

import java.time.LocalDateTime;

/** 어드민 모더레이션 — 혼자 북톡(기록) 한 건. */
public record AdminBookNoteItem(
        Long id,
        Long userId,
        String userNickname,
        String content,
        String imageUrl,
        LocalDateTime createdAt
) {
    public static AdminBookNoteItem from(BookNote n) {
        return new AdminBookNoteItem(
                n.getId(),
                n.getUser().getId(),
                n.getUser().getNickname(),
                n.getContent(),
                n.getImageUrl(),
                n.getCreatedAt());
    }
}
