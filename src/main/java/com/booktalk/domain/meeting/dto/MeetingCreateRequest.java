package com.booktalk.domain.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 모임 생성 요청.
 * 프런트 '모임 만들기' 화면은 readingMode/bookId/name 만 보낸다.
 * 생성자는 역할 구분 없이 첫 멤버(겸 host)가 된다.
 * capacity, recruitDeadline은 생략 가능하며 서버에서 기본값을 채운다.
 */
public record MeetingCreateRequest(
		@NotBlank(message = "읽는 방식(readingMode)은 필수입니다.") String readingMode, // TOGETHER | SOLO
		@NotNull(message = "bookId는 필수입니다.") Long bookId,
		@NotBlank(message = "모임 이름은 필수입니다.") String name,
		String visibility,         // PUBLIC | PRIVATE, 생략 시 PUBLIC(공개)
		Integer capacity,          // 생략 시 기본 정원
		LocalDate recruitDeadline  // 생략 시 오늘 + 기본 모집기간
) {
}
