package com.booktalk.domain.meeting.chat.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 채팅 WebSocket 엔드포인트 등록. ws(s)://host/ws/chat?meetingId=..&token=.. 로 연결한다.
 * 인증/인가는 {@link JwtHandshakeInterceptor}에서 핸드셰이크 단계에 수행한다.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

	private final ChatWebSocketHandler chatWebSocketHandler;
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(chatWebSocketHandler, "/ws/chat")
				.addInterceptors(jwtHandshakeInterceptor)
				.setAllowedOriginPatterns("*");
	}
}
