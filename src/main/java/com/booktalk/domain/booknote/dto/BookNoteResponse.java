package com.booktalk.domain.booknote.dto;

import com.booktalk.domain.book.dto.BookResponse;
import com.booktalk.domain.booknote.BookNote;

import java.time.LocalDateTime;

public record BookNoteResponse(
        Long id,
        BookResponse book,
        String imageUrl,
        String content,
        LocalDateTime createdAt
) {
    public static BookNoteResponse from(BookNote note) {
        return new BookNoteResponse(
                note.getId(),
                note.getBook() != null ? BookResponse.from(note.getBook()) : null,
                note.getImageUrl(),
                note.getContent(),
                note.getCreatedAt()
        );
    }
}
