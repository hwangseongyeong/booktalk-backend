package com.booktalk.domain.meeting.chat;

import com.booktalk.domain.meeting.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingChatMessageRepository extends JpaRepository<MeetingChatMessage, Long> {

	// 모임의 전체 메시지(오래된 → 최신)
	List<MeetingChatMessage> findByMeetingOrderByIdAsc(Meeting meeting);

	// 폴링용: 특정 id 이후의 새 메시지만
	List<MeetingChatMessage> findByMeetingAndIdGreaterThanOrderByIdAsc(Meeting meeting, Long afterId);

	// 리액션 폴링 윈도우 계산용: 모임의 최신 메시지 id
	@Query("select max(m.id) from MeetingChatMessage m where m.meeting = :meeting")
	Long findMaxIdByMeeting(@Param("meeting") Meeting meeting);
}
