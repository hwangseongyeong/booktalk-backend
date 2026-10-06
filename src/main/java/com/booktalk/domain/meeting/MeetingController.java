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

	/** 모임 참여 */
	@PostMapping("/{id}/join")
	public ApiResponse<MeetingResponse> join(@PathVariable Long id) {
		return ApiResponse.success(meetingService.join(id));
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
