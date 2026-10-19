package com.booktalk.domain.meeting.chat.realtime;

import com.booktalk.domain.meeting.MeetingMemberRepository;
import com.booktalk.global.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 핸드셰이크 시 JWT와 모임 참여 여부를 검증한다.
 * 쿼리 파라미터 token(access token), meetingId를 받아 검증하고,
 * 통과하면 세션 속성에 userId/meetingId를 심어 핸들러가 사용하게 한다. 실패 시 핸드셰이크를 거절한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtTokenProvider jwtTokenProvider;
	private final MeetingMemberRepository meetingMemberRepository;

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes
	) {
		if (!(request instanceof ServletServerHttpRequest servletRequest)) {
			return false;
		}
		HttpServletRequest req = servletRequest.getServletRequest();
		String token = req.getParameter("token");
		String meetingIdParam = req.getParameter("meetingId");
		if (token == null || meetingIdParam == null) {
			return false;
		}
		try {
			Long userId = jwtTokenProvider.parseUserIdFromAccessToken(token);
			Long meetingId = Long.parseLong(meetingIdParam);
			if (!meetingMemberRepository.existsByMeeting_IdAndUser_Id(meetingId, userId)) {
				return false; // 참여 중인 모임이 아니면 연결 거절
			}
			attributes.put(ChatWebSocketHandler.ATTR_USER_ID, userId);
			attributes.put(ChatWebSocketHandler.ATTR_MEETING_ID, meetingId);
			return true;
		} catch (Exception e) {
			log.debug("WS 핸드셰이크 인증 실패: {}", e.getMessage());
			return false;
		}
	}

	@Override
	public void afterHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Exception exception
	) {
		// no-op
	}
}
