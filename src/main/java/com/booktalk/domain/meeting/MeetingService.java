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

	/** 모임 생성. 생성자는 선택한 역할(기본 리더)로 첫 멤버가 된다. */
	@Transactional
	public MeetingResponse create(MeetingCreateRequest request) {
		User host = currentUserResolver.getCurrentUser();
		Book book = bookRepository.findById(request.bookId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다. id=" + request.bookId()));

		Meeting.ReadingMode mode = parseReadingMode(request.readingMode());
		MeetingMember.MemberRole hostRole = parseRole(request.role(), MeetingMember.MemberRole.LEADER);

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
				.capacity(capacity)
				.recruitDeadline(deadline)
				.build();

		MeetingMember hostMember = MeetingMember.builder()
				.meeting(meeting)
				.user(host)
				.role(hostRole)
				.build();
		meeting.addMember(hostMember); // cascade로 모임과 함께 저장된다

		Meeting saved = meetingRepository.save(meeting);
		return MeetingResponse.from(saved, hostRole);
	}

	/** 전체 모임 목록. status=RECRUITING|ONGOING|CLOSED 필터, 생략/ALL이면 전체. */
	public List<MeetingResponse> getMeetings(String status) {
		User me = currentUserResolver.getCurrentUser();

		List<Meeting> meetings = (status == null || status.isBlank() || status.equalsIgnoreCase("ALL"))
				? meetingRepository.findAllByOrderByIdDesc()
				: meetingRepository.findByStatusOrderByIdDesc(parseStatus(status));

		return meetings.stream()
				.map(meeting -> MeetingResponse.from(meeting, myRole(meeting, me)))
				.toList();
	}

	/** 내가 참여 중인 모임 목록(상단 '참여 중인 모임'). */
	public List<MeetingResponse> getJoinedMeetings() {
		User me = currentUserResolver.getCurrentUser();

		return meetingMemberRepository.findByUserOrderByMeetingIdDesc(me).stream()
				.map(member -> MeetingResponse.from(member.getMeeting(), member.getRole()))
				.toList();
	}

	public MeetingDetailResponse getMeeting(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);
		return MeetingDetailResponse.from(meeting, myRole(meeting, me));
	}

	/** 모임 참여(멤버로). */
	@Transactional
	public MeetingResponse join(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

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
				.role(MeetingMember.MemberRole.MEMBER)
				.build();
		meeting.addMember(member);
		meetingMemberRepository.save(member);

		return MeetingResponse.from(meeting, MeetingMember.MemberRole.MEMBER);
	}

	/** 모임 나가기. 리더는 나갈 수 없다(모임 종료/위임 필요). */
	@Transactional
	public void leave(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

		MeetingMember member = meetingMemberRepository.findByMeetingAndUser(meeting, me)
				.orElseThrow(() -> new IllegalStateException("참여하지 않은 모임입니다."));

		if (member.getRole() == MeetingMember.MemberRole.LEADER) {
			throw new IllegalStateException("리더는 모임을 나갈 수 없습니다. 모임을 종료하거나 리더를 위임해주세요.");
		}

		meeting.getMembers().remove(member);
		meetingMemberRepository.delete(member);
	}

	/** 모집 → 진행 중 전환(리더만). */
	@Transactional
	public MeetingResponse start(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

		MeetingMember.MemberRole role = myRole(meeting, me);
		if (role != MeetingMember.MemberRole.LEADER) {
			throw new IllegalStateException("모임 리더만 진행을 시작할 수 있습니다.");
		}

		if (meeting.getStatus() != Meeting.MeetingStatus.RECRUITING) {
			throw new IllegalStateException("모집 중인 모임만 진행을 시작할 수 있습니다.");
		}

		meeting.start();
		return MeetingResponse.from(meeting, role);
	}

	/** 모임 종료(리더만). 모집 중/진행 중 모임을 CLOSED로 전환한다. */
	@Transactional
	public MeetingResponse close(Long id) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

		MeetingMember.MemberRole role = myRole(meeting, me);
		if (role != MeetingMember.MemberRole.LEADER) {
			throw new IllegalStateException("모임 리더만 모임을 종료할 수 있습니다.");
		}
		if (meeting.getStatus() == Meeting.MeetingStatus.CLOSED) {
			throw new IllegalStateException("이미 종료된 모임입니다.");
		}

		meeting.close();
		return MeetingResponse.from(meeting, role);
	}

	/** 리더 위임. 현재 리더는 MEMBER로, 대상 멤버는 LEADER로 바뀐다. */
	@Transactional
	public MeetingResponse delegate(Long id, Long targetUserId) {
		User me = currentUserResolver.getCurrentUser();
		Meeting meeting = getMeetingOrThrow(id);

		MeetingMember leader = meetingMemberRepository.findByMeetingAndUser(meeting, me)
				.orElseThrow(() -> new IllegalStateException("참여하지 않은 모임입니다."));
		if (leader.getRole() != MeetingMember.MemberRole.LEADER) {
			throw new IllegalStateException("모임 리더만 리더를 위임할 수 있습니다.");
		}
		if (me.getId().equals(targetUserId)) {
			throw new IllegalArgumentException("자기 자신에게는 위임할 수 없습니다.");
		}

		MeetingMember target = meeting.getMembers().stream()
				.filter(m -> m.getUser().getId().equals(targetUserId))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("위임 대상이 모임 참여자가 아닙니다. userId=" + targetUserId));

		leader.changeRole(MeetingMember.MemberRole.MEMBER);
		target.changeRole(MeetingMember.MemberRole.LEADER);

		return MeetingResponse.from(meeting, MeetingMember.MemberRole.MEMBER);
	}

	// ---------- 내부 헬퍼 ----------
	private Meeting getMeetingOrThrow(Long id) {
		return meetingRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다. id=" + id));
	}

	/** 해당 모임에서 사용자의 역할. 참여자가 아니면 null. */
	private MeetingMember.MemberRole myRole(Meeting meeting, User user) {
		return meeting.getMembers().stream()
				.filter(m -> m.getUser().getId().equals(user.getId()))
				.map(MeetingMember::getRole)
				.findFirst()
				.orElse(null);
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

	private MeetingMember.MemberRole parseRole(String value, MeetingMember.MemberRole fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		try {
			return MeetingMember.MemberRole.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("역할(role)은 LEADER 또는 MEMBER 여야 합니다.");
		}
	}
}
