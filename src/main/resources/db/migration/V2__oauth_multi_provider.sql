-- 소셜 로그인 다중 제공자 지원을 위한 스키마 변경.
-- 기존에는 email 단일 유니크 키였지만, 제공자별 이메일 미제공 가능성(예: 카카오 비즈 미인증)이 있어
-- (oauth_provider, provider_id) 조합을 실제 유니크 키로 사용한다.

ALTER TABLE users
    ADD COLUMN provider_id VARCHAR(100) NULL AFTER oauth_provider;

-- 기존 데이터(있다면)는 임시 provider_id를 채워 NOT NULL 제약을 걸 수 있게 한다.
UPDATE users SET provider_id = CONCAT('legacy-', id) WHERE provider_id IS NULL;

ALTER TABLE users
    MODIFY COLUMN provider_id VARCHAR(100) NOT NULL;

ALTER TABLE users
    MODIFY COLUMN email VARCHAR(255) NULL;

ALTER TABLE users
    DROP INDEX email;

ALTER TABLE users
    ADD UNIQUE KEY uk_users_provider (oauth_provider, provider_id);
