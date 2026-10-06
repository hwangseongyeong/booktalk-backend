package com.booktalk.domain.meeting;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 독서 모임(소통). 한 권의 책을 함께/각자 읽는 모임.
 * 생성자(host)가 첫 멤버가 되며, 역할 구분 없이 모두 멤버다. 모임 종료/시작은 host만 할 수 있다.
 */
@Entity
@Table(name = "meetings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "host_id", nullable = false)
	private User host;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "reading_mode", nullable = false, length = 20)
	private ReadingMode readingMode; // TOGETHER(함께 읽기), SOLO(각자 읽기)

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MeetingStatus status; // RECRUITING, ONGOING, CLOSED

	@Column(nullable = false)
	private int capacity; // 정원

	// 모집 마감일. D-day 계산 기준. null이면 마감일 미설정.
	private LocalDate recruitDeadline;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	// 멤버는 모임 수명주기에 종속된다. 생성 시 host 멤버를 함께 저장하기 위해 cascade를 둔다.
	@OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
	private List<MeetingMember> members = new ArrayList<>();

	@Builder
	public Meeting(User host, Book book, String name, ReadingMode readingMode, int capacity, LocalDate recruitDeadline) {
		this.host = host;
		this.book = book;
		this.name = name;
		this.readingMode = readingMode;
		this.status = MeetingStatus.RECRUITING;
		this.capacity = capacity;
		this.recruitDeadline = recruitDeadline;
		this.createdAt = LocalDateTime.now();
	}

	public void addMember(MeetingMember member) {
		this.members.add(member);
	}

	public int getCurrentMemberCount() {
		return this.members.size();
	}

	public boolean isFull() {
		return this.members.size() >= this.capacity;
	}

	/** 모집 → 진행 중으로 전환(host만). */
	public void start() {
		this.status = MeetingStatus.ONGOING;
	}

	/** 모임 종료(host만). 모집 중이든 진행 중이든 종료할 수 있다. */
	public void close() {
		this.status = MeetingStatus.CLOSED;
	}

	/** 주어진 사용자가 이 모임의 생성자(host)인지. */
	public boolean isHostedBy(User user) {
		return this.host.getId().equals(user.getId());
	}

	public enum ReadingMode {
		TOGETHER, SOLO
	}

	public enum MeetingStatus {
		RECRUITING, ONGOING, CLOSED
	}
}
