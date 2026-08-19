package com.booktalk.domain.readingrecord.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReadingRecordStartRequest(
        @NotNull(message = "bookId는 필수입니다.") Long bookId,
        LocalDate startDate // 비어있으면 오늘 날짜로 처리
) {
}
