package com.booktalk.domain.meeting.chat.realtime;

import java.util.List;

/**
 * 메시지 리액션이 바뀐 뒤 발행되는 애플리케이션 이벤트.
 * mine 값은 수신자마다 다르므로, 브로드캐스트에는 집계 대신 이모지별 userId 목록을 담아
 * 각 클라이언트가 자신의 userId로 count/mine을 계산하게 한다.
 */
public record ChatReactionChangedEvent(Long meetingId, Long messageId, List<EmojiUserIds> reactions) {

	public record EmojiUserIds(String emoji, List<Long> userIds) {
	}
}
