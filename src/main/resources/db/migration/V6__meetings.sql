-- 독서 모임(소통) 도메인.
-- meetings: 모임 본체, meeting_members: 참여자(생성자 포함).

CREATE TABLE meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    host_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    reading_mode VARCHAR(20) NOT NULL,   -- TOGETHER, SOLO
    status VARCHAR(20) NOT NULL,         -- RECRUITING, ONGOING, CLOSED
    capacity INT NOT NULL,
    recruit_deadline DATE,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_meetings_host FOREIGN KEY (host_id) REFERENCES users(id),
    CONSTRAINT fk_meetings_book FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE INDEX idx_meetings_status ON meetings(status);

CREATE TABLE meeting_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,           -- LEADER, MEMBER
    joined_at DATETIME NOT NULL,
    CONSTRAINT fk_meeting_members_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id),
    CONSTRAINT fk_meeting_members_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_meeting_members_meeting_user UNIQUE (meeting_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE INDEX idx_meeting_members_user ON meeting_members(user_id);
