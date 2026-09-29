package com.booktalk.domain.readingrecord;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.user.User;
import com.booktalk.global.common.StringListConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

	// 완독 시 선택한 My Words 키워드(최대 3개). 콤마 구분 문자열로 단일 컬럼에 저장한다.
	@Convert(converter = StringListConverter.class)
	@Column(name = "my_words", length = 255)
	private List<String> myWords = new ArrayList<>();

	@Builder
	public ReadingRecord(User user, Book book, ReadingStatus status, LocalDate startDate) {
		this.user = user;
		this.book = book;
		this.status = status;
		this.startDate = startDate;
	}

	public void completeReading(LocalDate endDate, Double rating, String oneLineNote, List<String> myWords) {
		this.status = ReadingStatus.COMPLETED;
		this.endDate = endDate;
		this.rating = rating;
		this.oneLineNote = oneLineNote;
		this.myWords = (myWords != null) ? myWords : new ArrayList<>();
	}

	public enum ReadingStatus {
		READING, COMPLETED
	}
}
