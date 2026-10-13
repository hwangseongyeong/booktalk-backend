package com.booktalk.domain.monthlyshelf.dto;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.readingrecord.ReadingRecord;

import java.time.LocalDate;
import java.util.List;

public record ShelfBookItem(
        Long readingRecordId,
        Long bookId,
        String title,
        String author,
        String spineImageUrl,
        String primaryColor,
        LocalDate endDate,
        Double rating,
        String oneLineNote,
        List<String> myWords
) {
    public static ShelfBookItem from(ReadingRecord record) {
        Book book = record.getBook();
        return new ShelfBookItem(
                record.getId(),
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                // 책등은 저장하지 않고 실시간 렌더 — 실시간 엔드포인트 경로를 내려준다.
                "/api/v1/books/" + book.getId() + "/spine.svg",
                book.getPrimaryColor(),
                record.getEndDate(),
                record.getRating(),
                record.getOneLineNote(),
                record.getMyWords()
        );
    }
}
