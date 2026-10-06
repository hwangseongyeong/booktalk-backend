package com.booktalk.domain.meeting.dto;

import com.booktalk.domain.book.dto.BookResponse;
import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 모임 목록/요약 응답.
 * dday: 모집 마감까지 남은 일수(양수=남음, 0=오늘, 음수=지남, null=마감일 미설정).
 * isHost: 현재 로그인 사용자가 이 모임의 생성자인지(종료/시작 권한).
 * joined: 현재 로그인 사용자가 이 모임에 참여 중인지.
 */
public record MeetingResponse(
		Long id,
		String name,
		String readingMode,
		String status,
		String visibility,
		int capacity,
		int currentMemberCount,
		Long dday,
		LocalDate recruitDeadline,
		BookResponse book,
		boolean isHost,
		boolean joined,
		LocalDateTime createdAt
) {
	public static MeetingResponse from(Meeting meeting, User me) {
		Long dday = meeting.getRecruitDeadline() != null
				? ChronoUnit.DAYS.between(LocalDate.now(), meeting.getRecruitDeadline())
				: null;

		boolean isHost = meeting.isHostedBy(me);
		boolean joined = meeting.getMembers().stream()
				.anyMatch(m -> m.getUser().getId().equals(me.getId()));

		return new MeetingResponse(
				meeting.getId(),
				meeting.getName(),
				meeting.getReadingMode().name(),
				meeting.getStatus().name(),
				meeting.getVisibility().name(),
				meeting.getCapacity(),
				meeting.getCurrentMemberCount(),
				dday,
				meeting.getRecruitDeadline(),
				BookResponse.from(meeting.getBook()),
				isHost,
				joined,
				meeting.getCreatedAt()
		);
	}
}
