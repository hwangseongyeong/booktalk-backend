ALTER TABLE reading_records
    ADD CONSTRAINT uq_reading_records_user_book UNIQUE (user_id, book_id);