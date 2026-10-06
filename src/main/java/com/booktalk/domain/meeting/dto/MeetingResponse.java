package com.booktalk.domain.meeting.dto;

import com.booktalk.domain.book.dto.BookResponse;
import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.meeting.MeetingMember;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 모임 목록/요약 응답.
 * dday: 모집 마감까지 남은 일수(양수=남음, 0=오늘, 음수=지남, null=마감일 미설정).
 * myRole: 현재 로그인 사용자의 역할(LEADER/MEMBER). 참여하지 않았으면 null.
 */
public record MeetingResponse(
		Long id,
		String name,
		String readingMode,
		String status,
		int capacity,
		int currentMemberCount,
		Long dday,
		LocalDate recruitDeadline,
		BookResponse book,
		String myRole,
		LocalDateTime createdAt
) {
	public static MeetingResponse from(Meeting meeting, MeetingMember.MemberRole myRole) {
		Long dday = meeting.getRecruitDeadline() != null
				? ChronoUnit.DAYS.between(LocalDate.now(), meeting.getRecruitDeadline())
				: null;

		return new MeetingResponse(
				meeting.getId(),
				meeting.getName(),
				meeting.getReadingMode().name(),
				meeting.getStatus().name(),
				meeting.getCapacity(),
				meeting.getCurrentMemberCount(),
				dday,
				meeting.getRecruitDeadline(),
				BookResponse.from(meeting.getBook()),
				myRole != null ? myRole.name() : null,
				meeting.getCreatedAt()
		);
	}
}
