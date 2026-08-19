package com.booktalk.domain.readingrecord.dto;

import java.time.LocalDate;

public record ReadingRecordCompleteRequest(
        LocalDate endDate, // 비어있으면 오늘 날짜로 처리
        Double rating,
        String oneLineNote
) {
}
