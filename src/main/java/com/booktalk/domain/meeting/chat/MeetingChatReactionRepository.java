package com.booktalk.domain.meeting.chat;

import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingChatReactionRepository extends JpaRepository<MeetingChatReaction, Long> {

	// 특정 사용자가 특정 메시지에 남긴 리액션(토글/변경용)
	Optional<MeetingChatReaction> findByMessageAndUser(MeetingChatMessage message, User user);

	// 여러 메시지의 리액션 일괄 조회(N+1 방지)
	List<MeetingChatReaction> findByMessageIn(List<MeetingChatMessage> messages);

	// 단일 메시지의 리액션
	List<MeetingChatReaction> findByMessage(MeetingChatMessage message);

	// 모임 전체의 리액션(폴링용). mine 계산을 위해 user를 함께 가져온다.
	@Query("select r from MeetingChatReaction r join fetch r.user where r.message.meeting = :meeting")
	List<MeetingChatReaction> findAllByMeeting(@Param("meeting") Meeting meeting);
}
