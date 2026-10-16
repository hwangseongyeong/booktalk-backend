package com.booktalk.domain.meeting.chat;

import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.meeting.MeetingMemberRepository;
import com.booktalk.domain.meeting.MeetingRepository;
import com.booktalk.domain.meeting.chat.dto.MeetingChatMessageResponse;
import com.booktalk.domain.meeting.chat.dto.MeetingChatSendRequest;
import com.booktalk.domain.user.User;
import com.booktalk.global.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingChatService {

	private final MeetingRepository meetingRepository;
	private final MeetingMemberRepository meetingMemberRepository;
	private final MeetingChatMessageRepository chatRepository;
	private final CurrentUserResolver currentUserResolver;

	/** 모임 메시지 목록. afterId가 있으면 그 이후의 새 메시지만(폴링). 참여자만 조회 가능. */
	public List<MeetingChatMessageResponse> getMessages(Long meetingId, Long afterId) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getParticipatingMeeting(meetingId, me);

		List<MeetingChatMessage> messages = (afterId != null)
				? chatRepository.findByMeetingAndIdGreaterThanOrderByIdAsc(meeting, afterId)
				: chatRepository.findByMeetingOrderByIdAsc(meeting);

		return messages.stream()
				.map(m -> MeetingChatMessageResponse.from(m, me.getId()))
				.toList();
	}

	/** 메시지 전송. 참여자만 가능. */
	@Transactional
	public MeetingChatMessageResponse send(Long meetingId, MeetingChatSendRequest request) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getParticipatingMeeting(meetingId, me);

		String content = request.content() != null ? request.content().strip() : "";
		if (content.isEmpty()) {
			throw new IllegalArgumentException("메시지를 입력해주세요.");
		}

		MeetingChatMessage saved = chatRepository.save(
				MeetingChatMessage.builder()
						.meeting(meeting)
						.user(me)
						.content(content)
						.build());

		return MeetingChatMessageResponse.from(saved, me.getId());
	}

	/** 참여 중인 모임만 반환. host도 생성 시 멤버로 등록되므로 멤버십 검사로 충분하다. */
	private Meeting getParticipatingMeeting(Long meetingId, User me) {
		Meeting meeting = meetingRepository.findById(meetingId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. id=" + meetingId));
		if (!meetingMemberRepository.existsByMeetingAndUser(meeting, me)) {
			throw new IllegalStateException("참여 중인 모임에서만 대화할 수 있습니다.");
		}
		return meeting;
	}
}
