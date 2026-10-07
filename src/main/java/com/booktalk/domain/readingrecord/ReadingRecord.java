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

	// 완독 시 선택한 '나의 단어' — 감정/분위기/장르에서 각각 하나씩.
	private String emotion;   // 감정 (예: 따뜻한)
	private String mood;      // 분위기 (예: 매혹적인)
	private String genre;     // 장르 (예: 시)

	// 완독량. ALL(전체 읽었어요) / PARTIAL(일부 읽었어요)
	@Column(name = "read_amount", length = 20)
	private String readAmount;

	// 나의 단어(감정+분위기+장르)를 평면 목록으로도 보관한다. 서재/홈/북박스 표시에 재사용.
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

	/** 읽고 싶은 책(WISHLIST) → 읽는 중(READING)으로 전환. */
	public void startReading(LocalDate startDate) {
		this.status = ReadingStatus.READING;
		this.startDate = startDate;
	}

	public void completeReading(LocalDate endDate, Double rating, String oneLineNote,
			String emotion, String mood, String genre, String readAmount) {
		this.status = ReadingStatus.COMPLETED;
		this.endDate = endDate;
		this.rating = rating;
		this.oneLineNote = oneLineNote;
		this.emotion = emotion;
		this.mood = mood;
		this.genre = genre;
		this.readAmount = readAmount;

		List<String> words = new ArrayList<>();
		if (emotion != null && !emotion.isBlank()) words.add(emotion);
		if (mood != null && !mood.isBlank()) words.add(mood);
		if (genre != null && !genre.isBlank()) words.add(genre);
		this.myWords = words;
	}

	public enum ReadingStatus {
		WISHLIST, READING, COMPLETED
	}
}
