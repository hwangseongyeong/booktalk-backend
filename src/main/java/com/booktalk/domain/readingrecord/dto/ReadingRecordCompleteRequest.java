package com.booktalk.domain.readingrecord.dto;

import java.time.LocalDate;
import java.util.List;

public record ReadingRecordCompleteRequest(
        LocalDate endDate, // 비어있으면 오늘 날짜로 처리
        Double rating,
        String oneLineNote,
        List<String> myWords // 선택한 My Words 키워드(최대 3개)
) {
}
