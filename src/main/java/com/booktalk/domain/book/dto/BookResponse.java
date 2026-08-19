package com.booktalk.domain.book.dto;

import com.booktalk.domain.book.Book;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String author,
        String publisher,
        String coverImageUrl,
        Integer pageCount,
        String spineImageUrl,
        String primaryColor,
        String accentColor
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getCoverImageUrl(),
                book.getPageCount(),
                book.getSpineImageUrl(),
                book.getPrimaryColor(),
                book.getAccentColor()
        );
    }
}
