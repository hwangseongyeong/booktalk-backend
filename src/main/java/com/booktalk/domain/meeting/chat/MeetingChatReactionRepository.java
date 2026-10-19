package com.booktalk.domain.meeting.chat;

import com.booktalk.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingChatReactionRepository extends JpaRepository<MeetingChatReaction, Long> {

	// 특정 사용자가 특정 메시지에 남긴 리액션(토글/변경용)
	Optional<MeetingChatReaction> findByMessageAndUser(MeetingChatMessage message, User user);

	// 여러 메시지의 리액션 일괄 조회(N+1 방지)
	List<MeetingChatReaction> findByMessageIn(List<MeetingChatMessage> messages);

	// 단일 메시지의 리액션
	List<MeetingChatReaction> findByMessage(MeetingChatMessage message);
}
