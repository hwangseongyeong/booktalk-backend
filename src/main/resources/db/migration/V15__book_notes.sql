-- 혼자 기록(book_notes) 도메인.
-- 사용자가 책을 읽다가 남기는 개인 기록(사진/글). 책(book_id)은 선택.

CREATE TABLE book_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT,
    image_url VARCHAR(500),
    content TEXT,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_book_notes_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_book_notes_book FOREIGN KEY (book_id) REFERENCES books(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE INDEX idx_book_notes_user ON book_notes(user_id);
