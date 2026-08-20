package com.booktalk.domain.book.spine;

/**
 * 책등 이미지(SVG) 저장소 추상화.
 * 구현체는 storage.mode 설정값에 따라 하나만 활성화된다 (LocalSpineStorage | R2SpineStorage).
 */
public interface SpineStorage {

    /** 업로드 성공 시 공개 URL, 실패 시 null (호출부에서 흡수하고 색상만 채운다). */
    String upload(Long bookId, byte[] svgContent);
}
