package com.booktalk.domain.meeting.chat;

import com.booktalk.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅 메시지에 대한 이모지 리액션. 사용자당 메시지 1개(이모지는 변경 가능).
 * 참여자만 남길 수 있다(검증은 서비스에서).
 */
@Entity
@Table(
		name = "meeting_chat_reactions",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_meeting_chat_reaction_message_user",
				columnNames = {"message_id", "user_id"}
		)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingChatReaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "message_id", nullable = false)
	private MeetingChatMessage message;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 16)
	private String emoji;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Builder
	public MeetingChatReaction(MeetingChatMessage message, User user, String emoji) {
		this.message = message;
		this.user = user;
		this.emoji = emoji;
		this.createdAt = LocalDateTime.now();
	}

	/** 이모지 변경(사용자가 같은 메시지에 다른 이모지를 누른 경우). */
	public void changeEmoji(String emoji) {
		this.emoji = emoji;
		this.createdAt = LocalDateTime.now();
	}
}
