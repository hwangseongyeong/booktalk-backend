package com.booktalk.domain.book.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 도서 등록 요청.
 * 현재는 알라딘 API 검색 연동 전이므로 사용자가 직접 입력한 값을 그대로 등록한다.
 */
public record BookRegisterRequest(
        String isbn,
        @NotBlank(message = "제목은 필수입니다.") String title,
        String author,
        String publisher,
        String coverImageUrl,
        Integer pageCount
) {
}
