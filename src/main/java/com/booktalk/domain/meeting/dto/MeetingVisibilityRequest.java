package com.booktalk.domain.meeting.dto;

import jakarta.validation.constraints.NotBlank;

/** 모임 공개 범위 변경 요청. */
public record MeetingVisibilityRequest(
		@NotBlank(message = "visibility는 필수입니다.") String visibility // PUBLIC | PRIVATE
) {
}
