package com.booktalk.domain.monthlyshelf.dto;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.readingrecord.ReadingRecord;

import java.time.LocalDate;

public record ShelfBookItem(
        Long readingRecordId,
        Long bookId,
        String title,
        String author,
        String spineImageUrl,
        String primaryColor,
        LocalDate endDate,
        Double rating,
        String oneLineNote
) {
    public static ShelfBookItem from(ReadingRecord record) {
        Book book = record.getBook();
        return new ShelfBookItem(
                record.getId(),
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getSpineImageUrl(),
                book.getPrimaryColor(),
                record.getEndDate(),
                record.getRating(),
                record.getOneLineNote()
        );
    }
}
