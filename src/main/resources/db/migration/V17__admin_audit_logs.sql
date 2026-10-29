-- 어드민 민감 작업(삭제·정지·권한 변경 등) 감사 로그.
-- 누가(admin_user_id) · 무엇을(action) · 어떤 대상에(target_type/target_id) · 언제(created_at).
CREATE TABLE admin_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,         -- DELETE_BOOK_NOTE, SUSPEND_USER, GRANT_ADMIN ...
    target_type VARCHAR(50) NOT NULL,    -- BOOK_NOTE, MEETING_MESSAGE, USER ...
    target_id VARCHAR(100),
    detail VARCHAR(500),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_audit_admin FOREIGN KEY (admin_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE INDEX idx_audit_admin ON admin_audit_logs(admin_user_id);
CREATE INDEX idx_audit_created ON admin_audit_logs(created_at);
