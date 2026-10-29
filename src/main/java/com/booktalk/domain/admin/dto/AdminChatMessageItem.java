package com.booktalk.domain.admin.dto;

import com.booktalk.domain.meeting.chat.MeetingChatMessage;

import java.time.LocalDateTime;

/** 어드민 모더레이션 — 모임 채팅 메시지 한 건(삭제 포함 노출). */
public record AdminChatMessageItem(
        Long id,
        Long meetingId,
        Long userId,
        String userNickname,
        String content,
        boolean deleted,
        LocalDateTime createdAt
) {
    public static AdminChatMessageItem from(MeetingChatMessage m) {
        return new AdminChatMessageItem(
                m.getId(),
                m.getMeeting().getId(),
                m.getUser().getId(),
                m.getUser().getNickname(),
                m.getContent(),
                m.isDeleted(),
                m.getCreatedAt());
    }
}
