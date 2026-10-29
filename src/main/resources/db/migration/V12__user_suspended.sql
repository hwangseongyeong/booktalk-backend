-- 회원 정지 플래그. 정지된 사용자는 로그인/토큰 발급이 차단된다(soft — 복구 가능).
-- 기존 사용자는 모두 정상(FALSE).
ALTER TABLE users ADD COLUMN suspended BOOLEAN NOT NULL DEFAULT FALSE;
