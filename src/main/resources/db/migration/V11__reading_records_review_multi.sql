-- 나의 단어(감정/분위기/장르)를 카테고리당 최대 3개 다중 선택으로 변경.
-- 단일 String 컬럼(emotion/mood/genre, VARCHAR(30))을 콤마 구분 목록(VARCHAR(255))으로 바꾸고 복수형으로 리네임한다.
-- 기존 단일 값은 1개짜리 목록으로 그대로 유효하다.
ALTER TABLE reading_records
    CHANGE COLUMN emotion emotions VARCHAR(255),
    CHANGE COLUMN mood moods VARCHAR(255),
    CHANGE COLUMN genre genres VARCHAR(255);
