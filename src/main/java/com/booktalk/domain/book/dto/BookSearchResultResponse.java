package com.booktalk.domain.book.dto;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.book.external.AladinBookInfo;

/**
 * 도서 검색 결과 한 건.
 * id가 있으면 이미 로컬 DB에 등록된 책(바로 읽기 시작 가능), null이면 알라딘에서만 찾은 책(등록부터 필요).
 */
public record BookSearchResultResponse(
        Long id,
        String source, // "LOCAL" | "ALADIN"
        String isbn,
        String title,
        String author,
        String publisher,
        String coverImageUrl
) {
    public static BookSearchResultResponse fromLocal(Book book) {
        return new BookSearchResultResponse(
                book.getId(),
                "LOCAL",
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getCoverImageUrl()
        );
    }

    public static BookSearchResultResponse fromAladin(AladinBookInfo info) {
        return new BookSearchResultResponse(
                null,
                "ALADIN",
                info.isbn(),
                info.title(),
                info.author(),
                info.publisher(),
                info.coverImageUrl()
        );
    }
}
