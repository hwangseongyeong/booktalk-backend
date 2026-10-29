package com.booktalk.domain.admin.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 어드민 목록 응답용 간단 페이지 래퍼. (Spring Page 직접 직렬화의 불안정성/경고를 피한다)
 */
public record AdminPage<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> AdminPage<T> of(Page<T> p) {
        return new AdminPage<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
