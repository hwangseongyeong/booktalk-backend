package com.booktalk.domain.meeting.chat.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 메시지 리액션 요청. 같은 이모지를 다시 누르면 취소(토글), 다른 이모지면 교체.
 */
public record MeetingChatReactRequest(
		@NotBlank(message = "이모지를 선택해주세요.")
		String emoji
) {
}
