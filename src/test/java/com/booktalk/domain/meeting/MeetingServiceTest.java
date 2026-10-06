package com.booktalk.domain.meeting;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.book.BookRepository;
import com.booktalk.domain.meeting.dto.MeetingResponse;
import com.booktalk.domain.user.User;
import com.booktalk.global.security.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 역할 구분 없는 모임의 권한 시나리오 검증.
 * 생성자(host)만 종료/시작할 수 있고, host는 모임을 나갈 수 없다(종료해야 함).
 * MySQL 전용 프로젝트라 DB 없이 돌릴 수 있도록 서비스 계층을 Mockito로 단위 테스트한다.
 */
@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

	@Mock
	private MeetingRepository meetingRepository;
	@Mock
	private MeetingMemberRepository meetingMemberRepository;
	@Mock
	private BookRepository bookRepository;
	@Mock
	private CurrentUserResolver currentUserResolver;

	@InjectMocks
	private MeetingService meetingService;

	private User host;
	private User member;
	private Meeting meeting;
	private MeetingMember hostMember;
	private MeetingMember normalMember;

	@BeforeEach
	void setUp() {
		host = userWithId(1L, "생성자");
		member = userWithId(2L, "멤버");

		Book book = Book.builder().title("테스트 책").build();
		meeting = Meeting.builder()
				.host(host)
				.book(book)
				.name("테스트 모임")
				.readingMode(Meeting.ReadingMode.TOGETHER)
				.capacity(6)
				.build();

		hostMember = MeetingMember.builder().meeting(meeting).user(host).build();
		normalMember = MeetingMember.builder().meeting(meeting).user(member).build();
		meeting.addMember(hostMember);
		meeting.addMember(normalMember);
	}

	@Test
	@DisplayName("생성자는 모임을 나갈 수 없다")
	void host_cannot_leave() {
		given(currentUserResolver.getCurrentUser()).willReturn(host);
		given(meetingRepository.findById(10L)).willReturn(Optional.of(meeting));

		assertThatThrownBy(() -> meetingService.leave(10L))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("생성자는 모임을 나갈 수 없습니다");
	}

	@Test
	@DisplayName("일반 멤버는 모임을 나갈 수 있다")
	void member_can_leave() {
		given(currentUserResolver.getCurrentUser()).willReturn(member);
		given(meetingRepository.findById(10L)).willReturn(Optional.of(meeting));
		given(meetingMemberRepository.findByMeetingAndUser(meeting, member))
				.willReturn(Optional.of(normalMember));

		meetingService.leave(10L);

		assertThat(meeting.getMembers()).doesNotContain(normalMember);
		assertThat(meeting.getMembers()).contains(hostMember);
		assertThat(meeting.getCurrentMemberCount()).isEqualTo(1);
		verify(meetingMemberRepository).delete(normalMember);
	}

	@Test
	@DisplayName("생성자만 모임을 종료할 수 있다")
	void only_host_can_close() {
		given(currentUserResolver.getCurrentUser()).willReturn(member);
		given(meetingRepository.findById(10L)).willReturn(Optional.of(meeting));

		assertThatThrownBy(() -> meetingService.close(10L))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("생성자만 모임을 종료할 수 있습니다");
	}

	@Test
	@DisplayName("생성자는 모임을 종료할 수 있다")
	void host_can_close() {
		given(currentUserResolver.getCurrentUser()).willReturn(host);
		given(meetingRepository.findById(10L)).willReturn(Optional.of(meeting));

		MeetingResponse closed = meetingService.close(10L);

		assertThat(meeting.getStatus()).isEqualTo(Meeting.MeetingStatus.CLOSED);
		assertThat(closed.status()).isEqualTo("CLOSED");
		assertThat(closed.isHost()).isTrue();
		assertThat(closed.joined()).isTrue();
	}

	private User userWithId(Long id, String nickname) {
		User user = User.builder()
				.nickname(nickname)
				.oauthProvider("KAKAO")
				.providerId("p" + id)
				.build();
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}
}
