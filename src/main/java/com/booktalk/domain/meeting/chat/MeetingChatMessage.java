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

	// 첨부만 있는 메시지(사진/파일)를 허용하므로 nullable. 본문·첨부 중 하나는 있어야 함(검증은 서비스).
	@Column(length = 1000)
	private String content;

	// ----- 첨부(사진/파일) -----
	@Column(length = 512)
	private String attachmentUrl;

	@Column(length = 16)
	private String attachmentType; // IMAGE | FILE

	@Column(length = 255)
	private String attachmentName; // 원본 파일명

	private Long attachmentSize; // bytes

	@Column(nullable = false)
	private LocalDateTime createdAt;

	// 모더레이션 soft-delete. true면 대화방 조회에서 숨긴다(복구 가능).
	@Column(nullable = false)
	private boolean deleted = false;

	@Builder
	public MeetingChatMessage(Meeting meeting, User user, String content,
			String attachmentUrl, String attachmentType, String attachmentName, Long attachmentSize) {
		this.meeting = meeting;
		this.user = user;
		this.content = content;
		this.attachmentUrl = attachmentUrl;
		this.attachmentType = attachmentType;
		this.attachmentName = attachmentName;
		this.attachmentSize = attachmentSize;
		this.createdAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public void softDelete() {
		this.deleted = true;
	}
}
