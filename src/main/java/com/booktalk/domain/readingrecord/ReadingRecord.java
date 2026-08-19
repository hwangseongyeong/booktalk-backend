package com.booktalk.domain.readingrecord;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "reading_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadingRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReadingStatus status; // READING, COMPLETED

	private LocalDate startDate;

	private LocalDate endDate;

	private Double rating; // 0.0 ~ 5.0

	@Column(length = 500)
	private String oneLineNote;

	@Builder
	public ReadingRecord(User user, Book book, ReadingStatus status, LocalDate startDate) {
		this.user = user;
		this.book = book;
		this.status = status;
		this.startDate = startDate;
	}

	public void completeReading(LocalDate endDate, Double rating, String oneLineNote) {
		this.status = ReadingStatus.COMPLETED;
		this.endDate = endDate;
		this.rating = rating;
		this.oneLineNote = oneLineNote;
	}

	public enum ReadingStatus {
		READING, COMPLETED
	}
}
