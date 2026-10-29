-- 사용자 역할. 어드민 API(/api/v1/admin/**) 접근 권한의 기준.
-- 기존 사용자는 모두 일반(USER). 운영자는 별도로 ADMIN으로 승격한다.
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
