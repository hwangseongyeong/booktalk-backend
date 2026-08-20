package com.booktalk.domain.book.external;

/** 외부 API(카카오 책 검색 등)에서 가져온, 아직 우리 DB에는 없는 책 정보. */
public record ExternalBookInfo(
        String isbn,
        String title,
        String author,
        String publisher,
        String coverImageUrl
) {
}
