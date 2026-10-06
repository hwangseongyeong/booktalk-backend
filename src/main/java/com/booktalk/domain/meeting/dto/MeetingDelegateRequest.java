package com.booktalk.domain.meeting.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 리더 위임 요청. 위임 대상 멤버의 userId.
 */
public record MeetingDelegateRequest(
		@NotNull(message = "위임 대상(targetUserId)은 필수입니다.") Long targetUserId
) {
}
