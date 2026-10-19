package com.booktalk.domain.meeting.chat.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 채팅 실시간 수신 전용 WebSocket 핸들러.
 * 클라이언트는 수신만 하고(전송/리액션은 REST), 서버는 저장 후 이 세션들로 브로드캐스트한다.
 * 연결 시 핸드셰이크 인터셉터가 넣어둔 meetingId로 레지스트리에 등록한다.
 */
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

	public static final String ATTR_MEETING_ID = "meetingId";
	public static final String ATTR_USER_ID = "userId";

	private final ChatSessionRegistry registry;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		Long meetingId = (Long) session.getAttributes().get(ATTR_MEETING_ID);
		if (meetingId != null) {
			registry.register(meetingId, session);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		Long meetingId = (Long) session.getAttributes().get(ATTR_MEETING_ID);
		if (meetingId != null) {
			registry.unregister(meetingId, session);
		}
	}
}
