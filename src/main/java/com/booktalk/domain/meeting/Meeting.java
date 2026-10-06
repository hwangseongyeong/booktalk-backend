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
import java.util.UUID;

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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Visibility visibility; // PUBLIC(목록 노출+직접 참여), PRIVATE(목록 숨김+초대 토큰으로만 참여)

	@Column(nullable = false)
	private int capacity; // 정원

	// 모집 마감일. D-day 계산 기준. null이면 마감일 미설정.
	private LocalDate recruitDeadline;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	// 비공개 초대 토큰. 이 토큰이 담긴 링크로만 모임에 참여할 수 있다. host가 재발급해 기존 링크를 무효화할 수 있다.
	@Column(name = "invite_token", nullable = false, unique = true, length = 64)
	private String inviteToken;

	// 멤버는 모임 수명주기에 종속된다. 생성 시 host 멤버를 함께 저장하기 위해 cascade를 둔다.
	@OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
	private List<MeetingMember> members = new ArrayList<>();

	@Builder
	public Meeting(User host, Book book, String name, ReadingMode readingMode, Visibility visibility,
			int capacity, LocalDate recruitDeadline) {
		this.host = host;
		this.book = book;
		this.name = name;
		this.readingMode = readingMode;
		this.visibility = visibility != null ? visibility : Visibility.PUBLIC;
		this.status = MeetingStatus.RECRUITING;
		this.capacity = capacity;
		this.recruitDeadline = recruitDeadline;
		this.createdAt = LocalDateTime.now();
		this.inviteToken = generateInviteToken();
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

	/** 공개 모임인지(목록 노출 + 초대 없이 직접 참여 가능). */
	public boolean isPublic() {
		return this.visibility == Visibility.PUBLIC;
	}

	/** 초대 토큰 재발급(host만). 기존 초대 링크는 즉시 무효화된다. */
	public void reissueInviteToken() {
		this.inviteToken = generateInviteToken();
	}

	private static String generateInviteToken() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public enum ReadingMode {
		TOGETHER, SOLO
	}

	public enum MeetingStatus {
		RECRUITING, ONGOING, CLOSED
	}

	public enum Visibility {
		PUBLIC, PRIVATE
	}
}
