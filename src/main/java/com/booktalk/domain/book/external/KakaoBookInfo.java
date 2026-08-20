package com.booktalk.domain.book.external;

/** 카카오 책 검색 API에서 가져온, 아직 우리 DB에는 없는 책 정보. */
public record KakaoBookInfo(
        String isbn,
        String title,
        String author,
        String publisher,
        String coverImageUrl
) {
}
