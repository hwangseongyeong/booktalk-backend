-- 비공개 초대 토큰. 이 토큰이 담긴 링크로만 모임에 참여할 수 있다.
-- 1) nullable로 컬럼 추가 → 2) 기존 행을 UUID로 백필 → 3) NOT NULL + 유니크 제약.
ALTER TABLE meetings ADD COLUMN invite_token VARCHAR(64) NULL AFTER created_at;

UPDATE meetings SET invite_token = REPLACE(UUID(), '-', '') WHERE invite_token IS NULL;

ALTER TABLE meetings MODIFY COLUMN invite_token VARCHAR(64) NOT NULL;
ALTER TABLE meetings ADD CONSTRAINT uq_meetings_invite_token UNIQUE (invite_token);
