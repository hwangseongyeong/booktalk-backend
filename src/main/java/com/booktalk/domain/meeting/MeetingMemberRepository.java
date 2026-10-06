package com.booktalk.domain.meeting;

import com.booktalk.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingMemberRepository extends JpaRepository<MeetingMember, Long> {

	boolean existsByMeetingAndUser(Meeting meeting, User user);

	Optional<MeetingMember> findByMeetingAndUser(Meeting meeting, User user);

	// 내가 참여 중인 모임 목록(최근 참여 모임부터)
	List<MeetingMember> findByUserOrderByMeetingIdDesc(User user);
}
