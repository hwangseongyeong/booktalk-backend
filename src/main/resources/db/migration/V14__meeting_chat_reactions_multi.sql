-- 사용자가 한 메시지에 여러 이모지를 달 수 있도록 유니크 제약을 변경한다.
-- 기존: (message_id, user_id)  →  변경: (message_id, user_id, emoji)
-- message_id 외래키는 idx_meeting_chat_reaction_message_id(V13)가 있어 제약 교체 후에도 인덱스가 유지된다.

ALTER TABLE meeting_chat_reactions
    DROP INDEX uk_meeting_chat_reaction_message_user,
    ADD CONSTRAINT uk_meeting_chat_reaction_message_user_emoji UNIQUE (message_id, user_id, emoji);
