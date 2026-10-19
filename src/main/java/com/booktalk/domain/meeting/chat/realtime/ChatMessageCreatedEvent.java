package com.booktalk.domain.meeting.chat.realtime;

import com.booktalk.domain.meeting.chat.dto.MeetingChatMessageResponse;

/**
 * 새 채팅 메시지가 저장된 뒤 발행되는 애플리케이션 이벤트.
 * 트랜잭션 커밋 이후({@code AFTER_COMMIT}) WebSocket으로 브로드캐스트된다.
 */
public record ChatMessageCreatedEvent(Long meetingId, MeetingChatMessageResponse message) {
}
