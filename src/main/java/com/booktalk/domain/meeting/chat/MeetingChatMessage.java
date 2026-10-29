package com.booktalk.domain.meeting.chat;

import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 모임 참여자 간 채팅 메시지. 참여자(host 포함)만 작성할 수 있다(검증은 서비스에서).
 */
@Entity
@Table(name = "meeting_chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meeting_id", nullable = false)
	private Meeting meeting;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 1000)
	private String content;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	// 모더레이션 soft-delete. true면 대화방 조회에서 숨긴다(복구 가능).
	@Column(nullable = false)
	private boolean deleted = false;

	@Builder
	public MeetingChatMessage(Meeting meeting, User user, String content) {
		this.meeting = meeting;
		this.user = user;
		this.content = content;
		this.createdAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public void softDelete() {
		this.deleted = true;
	}
}
