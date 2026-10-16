package com.booktalk.domain.meeting.chat;

import com.booktalk.domain.meeting.chat.dto.MeetingChatMessageResponse;
import com.booktalk.domain.meeting.chat.dto.MeetingChatSendRequest;
import com.booktalk.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings/{meetingId}/messages")
@RequiredArgsConstructor
public class MeetingChatController {

	private final MeetingChatService chatService;

	/** 모임 메시지 목록. afterId가 있으면 그 이후 새 메시지만(폴링). */
	@GetMapping
	public ApiResponse<List<MeetingChatMessageResponse>> list(
			@PathVariable Long meetingId,
			@RequestParam(required = false) Long afterId
	) {
		return ApiResponse.success(chatService.getMessages(meetingId, afterId));
	}

	/** 메시지 전송 */
	@PostMapping
	public ApiResponse<MeetingChatMessageResponse> send(
			@PathVariable Long meetingId,
			@Valid @RequestBody MeetingChatSendRequest request
	) {
		return ApiResponse.success(chatService.send(meetingId, request));
	}
}
