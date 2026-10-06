-- 공개 범위. PUBLIC(목록 노출+직접 참여) / PRIVATE(목록 숨김+초대 토큰으로만 참여).
-- 기존 모임은 모두 공개(PUBLIC)로 둔다.
ALTER TABLE meetings ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' AFTER status;

CREATE INDEX idx_meetings_visibility_status ON meetings(visibility, status);
