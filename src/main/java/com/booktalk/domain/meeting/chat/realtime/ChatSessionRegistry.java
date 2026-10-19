package com.booktalk.domain.meeting.chat.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 모임별 WebSocket 세션 레지스트리. 메시지/리액션 브로드캐스트 대상 세션을 보관한다.
 *
 * 주의: 인메모리 레지스트리라 단일 인스턴스에서만 전체 참여자에게 전달된다.
 * 다중 인스턴스로 수평 확장할 때는 Redis Pub/Sub 등 외부 브로커로 브로드캐스트를 중계해야 한다.
 */
@Slf4j
@Component
public class ChatSessionRegistry {

	private final Map<Long, Set<WebSocketSession>> sessionsByMeeting = new ConcurrentHashMap<>();

	public void register(Long meetingId, WebSocketSession session) {
		sessionsByMeeting.computeIfAbsent(meetingId, k -> ConcurrentHashMap.newKeySet()).add(session);
	}

	public void unregister(Long meetingId, WebSocketSession session) {
		Set<WebSocketSession> sessions = sessionsByMeeting.get(meetingId);
		if (sessions != null) {
			sessions.remove(session);
			if (sessions.isEmpty()) {
				sessionsByMeeting.remove(meetingId);
			}
		}
	}

	/** 해당 모임의 모든 세션에 JSON 텍스트를 보낸다. 끊긴/오류 세션은 정리한다. */
	public void broadcast(Long meetingId, String payload) {
		Set<WebSocketSession> sessions = sessionsByMeeting.get(meetingId);
		if (sessions == null || sessions.isEmpty()) {
			return;
		}
		TextMessage message = new TextMessage(payload);
		for (WebSocketSession session : sessions) {
			if (!session.isOpen()) {
				sessions.remove(session);
				continue;
			}
			try {
				// WebSocketSession.sendMessage는 스레드 세이프하지 않으므로 세션 단위로 동기화한다.
				synchronized (session) {
					session.sendMessage(message);
				}
			} catch (IOException e) {
				log.debug("WS 전송 실패, 세션 정리 (meetingId={}): {}", meetingId, e.getMessage());
				sessions.remove(session);
			}
		}
	}
}
