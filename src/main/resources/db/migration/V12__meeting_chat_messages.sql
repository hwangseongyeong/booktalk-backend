-- 모임 참여자 채팅 메시지.
-- 참여자(host 포함)만 읽기/쓰기 가능하며, 애플리케이션에서 멤버십을 검증한다.

CREATE TABLE meeting_chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_meeting_chat_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id),
    CONSTRAINT fk_meeting_chat_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 모임별 메시지 조회 + afterId 폴링(id 오름차순)용
CREATE INDEX idx_meeting_chat_meeting_id ON meeting_chat_messages(meeting_id, id);
