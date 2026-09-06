-- 로그인 후 온보딩(닉네임 직접 설정 / 프로필 색 / 완료 여부)을 위한 컬럼 추가.
-- 이전에는 닉네임/프로필이 소셜 제공자 값으로 매 로그인마다 덮어써졌지만,
-- 온보딩을 마친 사용자는 본인이 정한 값을 유지한다(AuthService에서 분기).

ALTER TABLE users
    ADD COLUMN profile_color VARCHAR(20) NULL AFTER profile_image_url;

ALTER TABLE users
    ADD COLUMN onboarding_completed TINYINT(1) NOT NULL DEFAULT 0 AFTER profile_color;

-- 이 마이그레이션 이전에 가입한 사용자는 온보딩 화면 없이 쓰던 사람이므로 완료 처리한다.
UPDATE users SET onboarding_completed = 1;
