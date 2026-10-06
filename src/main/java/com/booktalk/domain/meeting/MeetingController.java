package com.booktalk.domain.meeting;

import com.booktalk.domain.meeting.dto.MeetingCreateRequest;
import com.booktalk.domain.meeting.dto.MeetingDetailResponse;
import com.booktalk.domain.meeting.dto.MeetingResponse;
import com.booktalk.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

	private final MeetingService meetingService;

	/** 모임 생성 */
	@PostMapping
	public ApiResponse<MeetingResponse> create(@Valid @RequestBody MeetingCreateRequest request) {
		return ApiResponse.success(meetingService.create(request));
	}

	/** 전체 모임 목록. status=RECRUITING|ONGOING|CLOSED|ALL(기본) */
	@GetMapping
	public ApiResponse<List<MeetingResponse>> getMeetings(@RequestParam(required = false) String status) {
		return ApiResponse.success(meetingService.getMeetings(status));
	}

	/** 내가 참여 중인 모임 목록 */
	@GetMapping("/joined")
	public ApiResponse<List<MeetingResponse>> getJoined() {
		return ApiResponse.success(meetingService.getJoinedMeetings());
	}

	/** 모임 상세(참여자 목록 포함) */
	@GetMapping("/{id}")
	public ApiResponse<MeetingDetailResponse> getMeeting(@PathVariable Long id) {
		return ApiResponse.success(meetingService.getMeeting(id));
	}

	/** 공개 모임 직접 참여(비공개 모임은 초대 토큰 필요) */
	@PostMapping("/{id}/join")
	public ApiResponse<MeetingResponse> join(@PathVariable Long id) {
		return ApiResponse.success(meetingService.join(id));
	}

	/** 초대 토큰으로 모임 미리보기(참여 수락 화면용) */
	@GetMapping("/invite/{token}")
	public ApiResponse<MeetingDetailResponse> getByInviteToken(@PathVariable String token) {
		return ApiResponse.success(meetingService.getByInviteToken(token));
	}

	/** 초대 토큰으로 모임 참여(비공개: 토큰 링크로만 참여 가능) */
	@PostMapping("/invite/{token}/join")
	public ApiResponse<MeetingResponse> joinByInviteToken(@PathVariable String token) {
		return ApiResponse.success(meetingService.joinByInviteToken(token));
	}

	/** 초대 링크 재발급(생성자) */
	@PostMapping("/{id}/invite/reissue")
	public ApiResponse<MeetingDetailResponse> reissueInviteToken(@PathVariable Long id) {
		return ApiResponse.success(meetingService.reissueInviteToken(id));
	}

	/** 모임 나가기 */
	@DeleteMapping("/{id}/leave")
	public ApiResponse<Void> leave(@PathVariable Long id) {
		meetingService.leave(id);
		return ApiResponse.success();
	}

	/** 모집 → 진행 중 전환(리더) */
	@PostMapping("/{id}/start")
	public ApiResponse<MeetingResponse> start(@PathVariable Long id) {
		return ApiResponse.success(meetingService.start(id));
	}

	/** 모임 종료(리더) */
	@PostMapping("/{id}/close")
	public ApiResponse<MeetingResponse> close(@PathVariable Long id) {
		return ApiResponse.success(meetingService.close(id));
	}
}
