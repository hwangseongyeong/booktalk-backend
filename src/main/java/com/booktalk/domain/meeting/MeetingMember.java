package com.booktalk.domain.meeting;

import com.booktalk.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 모임 참여자. (meeting, user) 조합은 유일하다(같은 모임에 중복 참여 불가).
 */
@Entity
@Table(
		name = "meeting_members",
		uniqueConstraints = @UniqueConstraint(
				name = "uq_meeting_members_meeting_user",
				columnNames = {"meeting_id", "user_id"}
		)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meeting_id", nullable = false)
	private Meeting meeting;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private LocalDateTime joinedAt;

	@Builder
	public MeetingMember(Meeting meeting, User user) {
		this.meeting = meeting;
		this.user = user;
		this.joinedAt = LocalDateTime.now();
	}
}
