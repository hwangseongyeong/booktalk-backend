package com.booktalk.domain.meeting.dto;

import com.booktalk.domain.meeting.MeetingMember;
import com.booktalk.domain.user.User;

import java.time.LocalDateTime;

public record MeetingMemberResponse(
		Long userId,
		String nickname,
		String profileImageUrl,
		String profileColor,
		String role,
		LocalDateTime joinedAt
) {
	public static MeetingMemberResponse from(MeetingMember member) {
		User user = member.getUser();
		return new MeetingMemberResponse(
				user.getId(),
				user.getNickname(),
				user.getProfileImageUrl(),
				user.getProfileColor(),
				member.getRole().name(),
				member.getJoinedAt()
		);
	}
}
