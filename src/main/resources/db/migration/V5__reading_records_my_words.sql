-- 완독 시 선택한 My Words 키워드를 한줄평(one_line_note)과 분리하여 저장한다.
-- MVP 단계에서는 별도 테이블 대신 콤마 구분 문자열로 단일 컬럼에 저장한다.
ALTER TABLE reading_records
    ADD COLUMN my_words VARCHAR(255) AFTER one_line_note;
