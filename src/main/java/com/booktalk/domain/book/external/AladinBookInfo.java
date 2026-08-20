package com.booktalk.domain.book.external;

/** 알라딘에서 가져온, 아직 우리 DB에는 없는 책 정보. */
public record AladinBookInfo(
        String isbn,
        String title,
        String author,
        String publisher,
        String coverImageUrl
) {
}
