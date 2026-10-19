package com.booktalk.domain.meeting.chat.realtime;

import com.booktalk.domain.meeting.chat.dto.MeetingChatMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 채팅 이벤트를 트랜잭션 커밋 이후 WebSocket으로 브로드캐스트한다.
 * AFTER_COMMIT으로 처리해, 저장이 롤백되면 전송하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRealtimeListener {

	private final ChatSessionRegistry registry;
	private final ObjectMapper objectMapper;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onMessageCreated(ChatMessageCreatedEvent event) {
		broadcast(event.meetingId(), new MessageEnvelope("MESSAGE", event.message()));
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onReactionChanged(ChatReactionChangedEvent event) {
		broadcast(event.meetingId(),
				new ReactionEnvelope("REACTION", event.messageId(), event.reactions()));
	}

	private void broadcast(Long meetingId, Object envelope) {
		try {
			registry.broadcast(meetingId, objectMapper.writeValueAsString(envelope));
		} catch (JsonProcessingException e) {
			log.warn("WS 직렬화 실패 (meetingId={}): {}", meetingId, e.getMessage());
		}
	}

	private record MessageEnvelope(String type, MeetingChatMessageResponse message) {
	}

	private record ReactionEnvelope(
			String type, Long messageId, List<ChatReactionChangedEvent.EmojiUserIds> reactions) {
	}
}
