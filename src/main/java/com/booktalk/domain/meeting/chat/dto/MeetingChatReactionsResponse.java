package com.booktalk.domain.meeting.chat.dto;

import com.booktalk.domain.meeting.chat.MeetingChatReaction;
import com.booktalk.domain.meeting.chat.dto.MeetingChatMessageResponse.ReactionSummary;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 폴링용 리액션 집계 응답. 최근 메시지 윈도우(fromMessageId 이상)만 포함한다.
 * 프런트는 fromMessageId 이상인 메시지에 대해서만 리액션을 갱신/비우고, 그보다 오래된 메시지는
 * 마지막으로 알고 있던 값을 유지한다(윈도우 밖의 리액션을 실수로 지우지 않도록).
 */
public record MeetingChatReactionsResponse(
		Long fromMessageId,
		List<MessageReactions> messages
) {
	/** 메시지별 이모지 집계. */
	public record MessageReactions(Long messageId, List<ReactionSummary> reactions) {
	}

	public static MeetingChatReactionsResponse of(
			Long fromMessageId,
			List<MeetingChatReaction> reactions,
			Long currentUserId
	) {
		Map<Long, List<MeetingChatReaction>> byMessage = reactions.stream()
				.collect(Collectors.groupingBy(r -> r.getMessage().getId()));

		List<MessageReactions> messages = byMessage.entrySet().stream()
				.map(e -> new MessageReactions(
						e.getKey(), ReactionSummary.summarize(e.getValue(), currentUserId)))
				.toList();

		return new MeetingChatReactionsResponse(fromMessageId, messages);
	}
}
