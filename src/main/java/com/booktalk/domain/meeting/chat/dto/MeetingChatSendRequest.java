package com.booktalk.domain.meeting.chat.dto;

import jakarta.validation.constraints.Size;

/**
 * 메시지 전송 요청. 본문(content) 또는 첨부(attachmentUrl) 중 하나는 있어야 한다(검증은 서비스에서).
 * 첨부는 먼저 업로드 API(/uploads/images · /uploads/files)로 올린 뒤 그 URL/메타를 함께 보낸다.
 */
public record MeetingChatSendRequest(
		@Size(max = 1000, message = "메시지는 1000자 이내로 입력해주세요.")
		String content,

		@Size(max = 512, message = "첨부 URL이 올바르지 않습니다.")
		String attachmentUrl,

		@Size(max = 16, message = "첨부 유형이 올바르지 않습니다.")
		String attachmentType,

		@Size(max = 255, message = "파일명이 너무 깁니다.")
		String attachmentName,

		Long attachmentSize
) {
}
