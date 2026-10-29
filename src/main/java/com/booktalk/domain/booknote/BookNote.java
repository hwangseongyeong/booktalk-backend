package com.booktalk.domain.booknote;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 혼자 기록. 사용자가 책을 읽다가 남기는 개인 기록(사진/글).
 * 책(book)은 선택이며, 사진(imageUrl)과 글(content) 중 하나 이상이 있어야 한다.
 */
@Entity
@Table(name = "book_notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookNote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// 어떤 책에 대한 기록인지(선택). 책 없이 사진/글만 남길 수도 있다.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id")
	private Book book;

	// 촬영/업로드한 사진의 접근 URL(선택). 업로드 API가 내려준 상대경로를 그대로 저장한다.
	@Column(name = "image_url", length = 500)
	private String imageUrl;

	// 기록 본문(선택).
	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	// 모더레이션 soft-delete. true면 사용자 피드에서 숨긴다(복구 가능).
	@Column(nullable = false)
	private boolean deleted = false;

	@Builder
	public BookNote(User user, Book book, String imageUrl, String content) {
		this.user = user;
		this.book = book;
		this.imageUrl = imageUrl;
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
