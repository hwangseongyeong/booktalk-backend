package com.booktalk.domain.meeting.dto;

import com.booktalk.domain.book.dto.BookResponse;
import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 모임 상세 응답. 요약 정보에 참여자 목록을 더한다.
 * isHost/joined 의미는 MeetingResponse 와 같다.
 * inviteToken: 비공개 초대 링크 토큰. 참여자(멤버)에게만 내려주고, 비참여자에게는 null(노출 방지).
 */
public record MeetingDetailResponse(
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
		String hostNickname,
		String inviteToken,
		LocalDateTime createdAt,
		List<MeetingMemberResponse> members
) {
	public static MeetingDetailResponse from(Meeting meeting, User me) {
		Long dday = meeting.getRecruitDeadline() != null
				? ChronoUnit.DAYS.between(LocalDate.now(), meeting.getRecruitDeadline())
				: null;

		boolean isHost = meeting.isHostedBy(me);
		boolean joined = meeting.getMembers().stream()
				.anyMatch(m -> m.getUser().getId().equals(me.getId()));

		List<MeetingMemberResponse> members = meeting.getMembers().stream()
				.map(MeetingMemberResponse::from)
				.toList();

		return new MeetingDetailResponse(
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
				meeting.getHost().getNickname(),
				joined ? meeting.getInviteToken() : null,
				meeting.getCreatedAt(),
				members
		);
	}
}
