package com.booktalk.domain.meeting.chat.dto;

import com.booktalk.domain.meeting.chat.MeetingChatReaction;
import com.booktalk.domain.meeting.chat.dto.MeetingChatMessageResponse.ReactionSummary;

import java.util.List;

/**
 * 메시지별 리액션 집계. 폴링에서 기존 메시지의 리액션 변경분을 반영하는 데 쓴다.
 */
public record MeetingChatReactionsResponse(
		Long messageId,
		List<ReactionSummary> reactions
) {
	public static MeetingChatReactionsResponse of(
			Long messageId,
			List<MeetingChatReaction> reactions,
			Long currentUserId
	) {
		return new MeetingChatReactionsResponse(messageId, ReactionSummary.summarize(reactions, currentUserId));
	}
}
