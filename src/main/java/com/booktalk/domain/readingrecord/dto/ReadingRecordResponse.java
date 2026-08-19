package com.booktalk.domain.readingrecord.dto;

import com.booktalk.domain.book.dto.BookResponse;
import com.booktalk.domain.readingrecord.ReadingRecord;

import java.time.LocalDate;

public record ReadingRecordResponse(
        Long id,
        BookResponse book,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        Double rating,
        String oneLineNote
) {
    public static ReadingRecordResponse from(ReadingRecord record) {
        return new ReadingRecordResponse(
                record.getId(),
                BookResponse.from(record.getBook()),
                record.getStatus().name(),
                record.getStartDate(),
                record.getEndDate(),
                record.getRating(),
                record.getOneLineNote()
        );
    }
}
