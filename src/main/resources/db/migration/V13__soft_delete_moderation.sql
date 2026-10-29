-- 모더레이션 soft-delete 플래그. 운영자가 삭제하면 사용자 노출에서 숨긴다(복구 가능).
-- 기존 데이터는 모두 정상(FALSE).
ALTER TABLE book_notes ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE meeting_chat_messages ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
