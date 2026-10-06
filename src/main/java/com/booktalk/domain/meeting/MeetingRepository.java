package com.booktalk.domain.meeting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

	List<Meeting> findAllByOrderByIdDesc();

	List<Meeting> findByStatusOrderByIdDesc(Meeting.MeetingStatus status);
}
