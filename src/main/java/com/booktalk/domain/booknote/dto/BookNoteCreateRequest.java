package com.booktalk.domain.booknote.dto;

/**
 * 혼자 기록 생성 요청. 책(bookId)은 선택이며,
 * 사진(imageUrl)과 글(content) 중 하나 이상은 있어야 한다(서비스에서 검증).
 */
public record BookNoteCreateRequest(
        Long bookId,
        String imageUrl,
        String content
) {
}
