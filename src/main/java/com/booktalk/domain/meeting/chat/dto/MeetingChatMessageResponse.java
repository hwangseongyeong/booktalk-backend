package com.booktalk.domain.meeting.chat.dto;

import com.booktalk.domain.meeting.chat.MeetingChatMessage;
import com.booktalk.domain.user.User;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 응답. mine=true면 현재 사용자가 보낸 메시지(프런트에서 오른쪽 정렬 등).
 */
public record MeetingChatMessageResponse(
		Long id,
		Long senderId,
		String senderNickname,
		String senderProfileImageUrl,
		String senderProfileColor,
		String content,
		LocalDateTime createdAt,
		boolean mine
) {
	public static MeetingChatMessageResponse from(MeetingChatMessage message, Long currentUserId) {
		User sender = message.getUser();
		return new MeetingChatMessageResponse(
				message.getId(),
				sender.getId(),
				sender.getNickname(),
				sender.getProfileImageUrl(),
				sender.getProfileColor(),
				message.getContent(),
				message.getCreatedAt(),
				sender.getId().equals(currentUserId)
		);
	}
}
