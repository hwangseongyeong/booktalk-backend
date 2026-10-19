package com.booktalk.domain.meeting.chat;

import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingChatReactionRepository extends JpaRepository<MeetingChatReaction, Long> {

	// 특정 사용자가 특정 메시지에 남긴 특정 이모지 리액션(이모지별 토글용)
	Optional<MeetingChatReaction> findByMessageAndUserAndEmoji(MeetingChatMessage message, User user, String emoji);

	// 여러 메시지의 리액션 일괄 조회(N+1 방지)
	List<MeetingChatReaction> findByMessageIn(List<MeetingChatMessage> messages);

	// 단일 메시지의 리액션
	List<MeetingChatReaction> findByMessage(MeetingChatMessage message);

	// 폴링용: 최근 메시지 윈도우(fromMessageId 이상)의 리액션만. 전체 히스토리를 매번 긁지 않도록 바운드한다.
	// mine 계산을 위해 user를 함께 가져온다.
	@Query("select r from MeetingChatReaction r join fetch r.user "
			+ "where r.message.meeting = :meeting and r.message.id >= :fromMessageId")
	List<MeetingChatReaction> findByMeetingFromMessageId(
			@Param("meeting") Meeting meeting, @Param("fromMessageId") Long fromMessageId);
}
