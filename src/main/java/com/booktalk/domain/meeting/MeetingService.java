package com.booktalk.domain.meeting;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.book.BookRepository;
import com.booktalk.domain.meeting.dto.MeetingCreateRequest;
import com.booktalk.domain.meeting.dto.MeetingDetailResponse;
import com.booktalk.domain.meeting.dto.MeetingResponse;
import com.booktalk.domain.user.User;
import com.booktalk.global.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {

	private static final int DEFAULT_CAPACITY = 6;
	private static final int DEFAULT_RECRUIT_DAYS = 7;

	private final MeetingRepository meetingRepository;
	private final MeetingMemberRepository meetingMemberRepository;
	private final BookRepository bookRepository;
	private final CurrentUserResolver currentUserResolver;

	/** 모임 생성. 생성자(host)가 역할 구분 없이 첫 멤버가 된다. */
	@Transactional
	public MeetingResponse create(MeetingCreateRequest request) {
		User host = currentUserResolver.getCurrentUser();
		Book book = bookRepository.findById(request.bookId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다. id=" + request.bookId()));

		Meeting.ReadingMode mode = parseReadingMode(request.readingMode());
		Meeting.Visibility visibility = parseVisibility(request.visibility());

		int capacity = request.capacity() != null ? request.capacity() : DEFAULT_CAPACITY;
		if (capacity < 1) {
			throw new IllegalArgumentException("정원은 1명 이상이어야 합니다.");
		}
		LocalDate deadline = request.recruitDeadline() != null
				? request.recruitDeadline()
				: LocalDate.now().plusDays(DEFAULT_RECRUIT_DAYS);

		Meeting meeting = Meeting.builder()
				.host(host)
				.book(book)
				.name(request.name().strip())
				.readingMode(mode)
				.visibility(visibility)
				.capacity(capacity)
				.recruitDeadline(deadline)
				.build();

		MeetingMember hostMember = MeetingMember.builder()
				.meeting(meeting)
				.user(host)
				.build();
		meeting.addMember(hostMember); // cascade로 모임과 함께 저장된다

		Meeting saved = meetingRepository.save(meeting);
		return MeetingResponse.from(saved, host);
	}

	/** 공개 모임 목록. 비공개(PRIVATE) 모임은 숨긴다. status=RECRUITING|ONGOING|CLOSED 필터, 생략/ALL이면 전체. */
	public List<MeetingResponse> getMeetings(String status) {
		User me = currentUserResolver.getCurrentUser();

		List<Meeting> meetings = (status == null || status.isBlank() || status.equalsIgnoreCase("ALL"))
				? meetingRepository.findByVisibilityOrderByIdDesc(Meeting.Visibility.PUBLIC)
				: meetingRepository.findByVisibilityAndStatusOrderByIdDesc(Meeting.Visibility.PUBLIC, parseStatus(status));

		return meetings.stream()
				.map(meeting -> MeetingResponse.from(meeting, me))
				.toList();
	}

	/** 내가 참여 중인 모임 목록(상단 '참여 중인 모임'). */
	public List<MeetingResponse> getJoinedMeetings() {
		User me = currentUserResolver.getCurrentUser();

		return meetingMemberRepository.findByUserOrderByMeetingIdDesc(me).stream()
				.map(member -> MeetingResponse.from(member.getMeeting(), me))
				.toList();
	}

	public MeetingDetailResponse getMeeting(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);
		return MeetingDetailResponse.from(meeting, me);
	}

	/** 초대 토큰으로 모임 미리보기(참여 전 수락 화면용). */
	public MeetingDetailResponse getByInviteToken(String token) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingByTokenOrThrow(token);
		return MeetingDetailResponse.from(meeting, me);
	}

	/** 공개 모임 직접 참여. 비공개(PRIVATE) 모임은 초대 토큰으로만 참여할 수 있다. */
	@Transactional
	public MeetingResponse join(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

		if (!meeting.isPublic()) {
			throw new IllegalStateException("초대 링크로만 참여할 수 있는 모임입니다.");
		}

		return addMember(meeting, me);
	}

	/** 초대 토큰으로 모임 참여. 공개/비공개 모두 가능하다(유효한 토큰 링크 필요). */
	@Transactional
	public MeetingResponse joinByInviteToken(String token) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingByTokenOrThrow(token);
		return addMember(meeting, me);
	}

	/** 참여 가능 여부를 검증하고 멤버로 추가한다. */
	private MeetingResponse addMember(Meeting meeting, User me) {
		if (meeting.getStatus() == Meeting.MeetingStatus.CLOSED) {
			throw new IllegalStateException("종료된 모임입니다.");
		}
		if (meetingMemberRepository.existsByMeetingAndUser(meeting, me)) {
			throw new IllegalStateException("이미 참여 중인 모임입니다.");
		}
		if (meeting.isFull()) {
			throw new IllegalStateException("정원이 가득 찬 모임입니다.");
		}

		MeetingMember member = MeetingMember.builder()
				.meeting(meeting)
				.user(me)
				.build();
		meeting.addMember(member);
		meetingMemberRepository.save(member);

		return MeetingResponse.from(meeting, me);
	}

	/** 공개 범위 전환(host만). 비공개로 바꾸면 목록에서 숨겨지고 초대 토큰으로만 참여할 수 있다. */
	@Transactional
	public MeetingDetailResponse changeVisibility(Long id, String visibility) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

		if (!meeting.isHostedBy(me)) {
			throw new IllegalStateException("모임 생성자만 공개 범위를 변경할 수 있습니다.");
		}

		meeting.changeVisibility(parseVisibility(visibility));
		return MeetingDetailResponse.from(meeting, me);
	}

	/** 초대 토큰 재발급(host만). 기존 초대 링크는 즉시 무효화된다. */
	@Transactional
	public MeetingDetailResponse reissueInviteToken(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

		if (!meeting.isHostedBy(me)) {
			throw new IllegalStateException("모임 생성자만 초대 링크를 재발급할 수 있습니다.");
		}

		meeting.reissueInviteToken();
		return MeetingDetailResponse.from(meeting, me);
	}

	/** 모임 나가기. 생성자(host)는 나갈 수 없다(모임을 종료해야 함). */
	@Transactional
	public void leave(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

		if (meeting.isHostedBy(me)) {
			throw new IllegalStateException("모임 생성자는 모임을 나갈 수 없습니다. 모임을 종료해주세요.");
		}

		MeetingMember member = meetingMemberRepository.findByMeetingAndUser(meeting, me)
				.orElseThrow(() -> new IllegalStateException("참여하지 않은 모임입니다."));

		meeting.getMembers().remove(member);
		meetingMemberRepository.delete(member);
	}

	/** 모집 → 진행 중 전환(생성자만). */
	@Transactional
	public MeetingResponse start(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

		if (!meeting.isHostedBy(me)) {
			throw new IllegalStateException("모임 생성자만 진행을 시작할 수 있습니다.");
		}
		if (meeting.getStatus() != Meeting.MeetingStatus.RECRUITING) {
			throw new IllegalStateException("모집 중인 모임만 진행을 시작할 수 있습니다.");
		}

		meeting.start();
		return MeetingResponse.from(meeting, me);
	}

	/** 모임 종료(생성자만). 모집 중/진행 중 모임을 CLOSED로 전환한다. */
	@Transactional
	public MeetingResponse close(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

		if (!meeting.isHostedBy(me)) {
			throw new IllegalStateException("모임 생성자만 모임을 종료할 수 있습니다.");
		}
		if (meeting.getStatus() == Meeting.MeetingStatus.CLOSED) {
			throw new IllegalStateException("이미 종료된 모임입니다.");
		}

		meeting.close();
		return MeetingResponse.from(meeting, me);
	}

	// ---------- 내부 헬퍼 ----------
	private Meeting getMeetingOrThrow(Long id) {
		return meetingRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. id=" + id));
	}

	private Meeting getMeetingByTokenOrThrow(String token) {
		return meetingRepository.findByInviteToken(token)
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 링크입니다."));
	}

	private Meeting.ReadingMode parseReadingMode(String value) {
		try {
			return Meeting.ReadingMode.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new IllegalArgumentException("읽는 방식(readingMode)은 TOGETHER 또는 SOLO 여야 합니다.");
		}
	}

	private Meeting.MeetingStatus parseStatus(String value) {
		try {
			return Meeting.MeetingStatus.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new IllegalArgumentException("status는 RECRUITING, ONGOING, CLOSED, ALL 중 하나여야 합니다.");
		}
	}

	/** 공개 범위. 생략 시 PUBLIC(공개). */
	private Meeting.Visibility parseVisibility(String value) {
		if (value == null || value.isBlank()) {
			return Meeting.Visibility.PUBLIC;
		}
		try {
			return Meeting.Visibility.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("visibility는 PUBLIC 또는 PRIVATE 여야 합니다.");
		}
	}
}
