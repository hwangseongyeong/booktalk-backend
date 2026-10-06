package com.booktalk.domain.meeting.dto;

import com.booktalk.domain.book.dto.BookResponse;
import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.meeting.MeetingMember;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 모임 상세 응답. 요약 정보에 참여자 목록을 더한다.
 */
public record MeetingDetailResponse(
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
		LocalDateTime createdAt,
		List<MeetingMemberResponse> members
) {
	public static MeetingDetailResponse from(Meeting meeting, MeetingMember.MemberRole myRole) {
		Long dday = meeting.getRecruitDeadline() != null
				? ChronoUnit.DAYS.between(LocalDate.now(), meeting.getRecruitDeadline())
				: null;

		List<MeetingMemberResponse> members = meeting.getMembers().stream()
				.map(MeetingMemberResponse::from)
				.toList();

		return new MeetingDetailResponse(
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
				meeting.getCreatedAt(),
				members
		);
	}
}
