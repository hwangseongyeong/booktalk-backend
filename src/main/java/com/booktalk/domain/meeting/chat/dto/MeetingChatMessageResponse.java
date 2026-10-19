package com.booktalk.domain.meeting.chat.dto;

import com.booktalk.domain.meeting.chat.MeetingChatMessage;
import com.booktalk.domain.meeting.chat.MeetingChatReaction;
import com.booktalk.domain.user.User;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 채팅 메시지 응답. mine=true면 현재 사용자가 보낸 메시지(프런트에서 오른쪽 정렬 등).
 * reactions는 이모지별 집계(개수 + 내가 눌렀는지).
 */
public record MeetingChatMessageResponse(
		Long id,
		Long senderId,
		String senderNickname,
		String senderProfileImageUrl,
		String senderProfileColor,
		String content,
		LocalDateTime createdAt,
		boolean mine,
		List<ReactionSummary> reactions
) {
	/** 이모지별 리액션 집계. mine=true면 현재 사용자가 그 이모지를 눌렀다는 뜻. */
	public record ReactionSummary(String emoji, int count, boolean mine) {

		/** 이모지 등장 순서를 유지하며 개수를 집계하고, 현재 사용자의 선택 여부를 표시한다. */
		public static List<ReactionSummary> summarize(List<MeetingChatReaction> reactions, Long currentUserId) {
			if (reactions == null || reactions.isEmpty()) {
				return List.of();
			}
			Map<String, int[]> counts = new LinkedHashMap<>(); // emoji -> [count, mine(0/1)]
			for (MeetingChatReaction r : reactions) {
				int[] c = counts.computeIfAbsent(r.getEmoji(), k -> new int[2]);
				c[0]++;
				if (r.getUser().getId().equals(currentUserId)) {
					c[1] = 1;
				}
			}
			return counts.entrySet().stream()
					.map(e -> new ReactionSummary(e.getKey(), e.getValue()[0], e.getValue()[1] == 1))
					.toList();
		}
	}

	public static MeetingChatMessageResponse from(
			MeetingChatMessage message,
			Long currentUserId,
			List<MeetingChatReaction> reactions
	) {
		User sender = message.getUser();
		return new MeetingChatMessageResponse(
				message.getId(),
				sender.getId(),
				sender.getNickname(),
				sender.getProfileImageUrl(),
				sender.getProfileColor(),
				message.getContent(),
				message.getCreatedAt(),
				sender.getId().equals(currentUserId),
				ReactionSummary.summarize(reactions, currentUserId)
		);
	}
}
