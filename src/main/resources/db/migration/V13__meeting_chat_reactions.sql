-- 채팅 메시지 이모지 리액션.
-- 사용자당 메시지 1개 리액션(이모지는 변경 가능). 같은 이모지는 개수로 집계한다.
-- 참여자만 가능하며, 애플리케이션에서 멤버십을 검증한다.

CREATE TABLE meeting_chat_reactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    emoji VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_meeting_chat_reaction_message FOREIGN KEY (message_id) REFERENCES meeting_chat_messages(id),
    CONSTRAINT fk_meeting_chat_reaction_user FOREIGN KEY (user_id) REFERENCES users(id),
    -- 사용자당 메시지 1개 리액션만 허용(이모지 교체는 UPDATE)
    CONSTRAINT uk_meeting_chat_reaction_message_user UNIQUE (message_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 메시지별 리액션 일괄 조회용
CREATE INDEX idx_meeting_chat_reaction_message_id ON meeting_chat_reactions(message_id);
