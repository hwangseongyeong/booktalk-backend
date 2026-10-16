package com.booktalk.domain.meeting.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MeetingChatSendRequest(
		@NotBlank(message = "메시지를 입력해주세요.")
		@Size(max = 1000, message = "메시지는 1000자 이내로 입력해주세요.")
		String content
) {
}
