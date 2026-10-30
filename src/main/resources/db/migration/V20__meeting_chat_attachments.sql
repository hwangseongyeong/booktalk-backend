-- 모임 채팅에 사진/파일 첨부 지원.
-- 첨부만 있는 메시지(본문 없음)를 허용하기 위해 content를 nullable로 바꾸고, 첨부 메타 컬럼을 추가한다.
ALTER TABLE meeting_chat_messages MODIFY COLUMN content VARCHAR(1000) NULL;

ALTER TABLE meeting_chat_messages
    ADD COLUMN attachment_url  VARCHAR(512) NULL,  -- 업로드된 파일 접근 URL(이미지는 /uploads/images, 파일은 /uploads/files)
    ADD COLUMN attachment_type VARCHAR(16)  NULL,  -- IMAGE | FILE
    ADD COLUMN attachment_name VARCHAR(255) NULL,  -- 원본 파일명(다운로드 표시/파일명용)
    ADD COLUMN attachment_size BIGINT       NULL;  -- 바이트 단위 크기
