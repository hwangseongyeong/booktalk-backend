-- 모임에서 리더/멤버 역할 구분을 제거한다.
-- 모든 참여자는 역할 구분 없이 멤버이며, 종료/시작 권한은 생성자(meetings.host_id)로 판단한다.
ALTER TABLE meeting_members DROP COLUMN role;
