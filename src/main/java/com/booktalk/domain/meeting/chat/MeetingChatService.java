package com.booktalk.domain.meeting.chat;

import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.meeting.MeetingMemberRepository;
import com.booktalk.domain.meeting.MeetingRepository;
import com.booktalk.domain.meeting.chat.dto.MeetingChatMessageResponse;
import com.booktalk.domain.meeting.chat.dto.MeetingChatReactRequest;
import com.booktalk.domain.meeting.chat.dto.MeetingChatSendRequest;
import com.booktalk.domain.user.User;
import com.booktalk.global.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingChatService {

	/** 리액션으로 허용되는 이모지(프런트 고정 팔레트와 동일). */
	private static final Set<String> ALLOWED_REACTIONS = Set.of("👍", "❤️", "😂", "😮", "😢");

	private final MeetingRepository meetingRepository;
	private final MeetingMemberRepository meetingMemberRepository;
	private final MeetingChatMessageRepository chatRepository;
	private final MeetingChatReactionRepository reactionRepository;
	private final CurrentUserResolver currentUserResolver;

	/** 모임 메시지 목록. afterId가 있으면 그 이후의 새 메시지만(폴링). 참여자만 조회 가능. */
	public List<MeetingChatMessageResponse> getMessages(Long meetingId, Long afterId) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getParticipatingMeeting(meetingId, me);

		List<MeetingChatMessage> messages = (afterId != null)
				? chatRepository.findByMeetingAndIdGreaterThanOrderByIdAsc(meeting, afterId)
				: chatRepository.findByMeetingOrderByIdAsc(meeting);

		Map<Long, List<MeetingChatReaction>> reactionsByMessage = loadReactions(messages);

		return messages.stream()
				.map(m -> MeetingChatMessageResponse.from(
						m, me.getId(), reactionsByMessage.getOrDefault(m.getId(), List.of())))
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

		// 새 메시지에는 아직 리액션이 없다.
		return MeetingChatMessageResponse.from(saved, me.getId(), List.of());
	}

	/**
	 * 메시지에 이모지 리액션을 토글한다. 참여자만 가능.
	 * 같은 이모지를 다시 누르면 취소, 다른 이모지면 교체, 없으면 추가.
	 */
	@Transactional
	public MeetingChatMessageResponse react(Long meetingId, Long messageId, MeetingChatReactRequest request) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getParticipatingMeeting(meetingId, me);

		String emoji = request.emoji() != null ? request.emoji().strip() : "";
		if (!ALLOWED_REACTIONS.contains(emoji)) {
			throw new IllegalArgumentException("사용할 수 없는 이모지입니다.");
		}

		MeetingChatMessage message = chatRepository.findById(messageId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메시지입니다. id=" + messageId));
		if (!message.getMeeting().getId().equals(meeting.getId())) {
			throw new IllegalArgumentException("해당 모임의 메시지가 아닙니다.");
		}

		Optional<MeetingChatReaction> existing = reactionRepository.findByMessageAndUser(message, me);
		if (existing.isPresent()) {
			MeetingChatReaction reaction = existing.get();
			if (reaction.getEmoji().equals(emoji)) {
				reactionRepository.delete(reaction); // 같은 이모지 → 토글 취소
			} else {
				reaction.changeEmoji(emoji); // 다른 이모지 → 교체
			}
		} else {
			reactionRepository.save(
					MeetingChatReaction.builder()
							.message(message)
							.user(me)
							.emoji(emoji)
							.build());
		}

		List<MeetingChatReaction> reactions = reactionRepository.findByMessage(message);
		return MeetingChatMessageResponse.from(message, me.getId(), reactions);
	}

	/** 메시지 묶음의 리액션을 한 번에 조회해 messageId별로 묶는다(N+1 방지). */
	private Map<Long, List<MeetingChatReaction>> loadReactions(List<MeetingChatMessage> messages) {
		if (messages.isEmpty()) {
			return Map.of();
		}
		return reactionRepository.findByMessageIn(messages).stream()
				.collect(Collectors.groupingBy(r -> r.getMessage().getId()));
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
