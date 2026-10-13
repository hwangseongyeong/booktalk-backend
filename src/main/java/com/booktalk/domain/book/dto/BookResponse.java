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
                // 책등은 저장하지 않고 실시간 렌더하므로, 저장된 URL 대신 실시간 엔드포인트 경로를 내려준다.
                "/api/v1/books/" + book.getId() + "/spine.svg",
                book.getPrimaryColor(),
                book.getAccentColor()
        );
    }
}
