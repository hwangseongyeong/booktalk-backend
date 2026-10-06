package com.booktalk.domain.meeting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

	List<Meeting> findByVisibilityOrderByIdDesc(Meeting.Visibility visibility);

	List<Meeting> findByVisibilityAndStatusOrderByIdDesc(Meeting.Visibility visibility, Meeting.MeetingStatus status);

	Optional<Meeting> findByInviteToken(String inviteToken);
}
