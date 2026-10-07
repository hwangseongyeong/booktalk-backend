-- 완독 리뷰의 '나의 단어'를 감정/분위기/장르로 구조화하고, 완독량을 함께 저장한다.
-- my_words 컬럼은 감정+분위기+장르를 합친 평면 목록으로 계속 유지(서재/홈/북박스 표시 호환).
ALTER TABLE reading_records
    ADD COLUMN emotion VARCHAR(30) AFTER my_words,
    ADD COLUMN mood VARCHAR(30) AFTER emotion,
    ADD COLUMN genre VARCHAR(30) AFTER mood,
    ADD COLUMN read_amount VARCHAR(20) AFTER genre;
