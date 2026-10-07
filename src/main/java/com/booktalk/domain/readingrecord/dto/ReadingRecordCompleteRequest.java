package com.booktalk.domain.readingrecord.dto;

import java.time.LocalDate;

public record ReadingRecordCompleteRequest(
        LocalDate endDate, // 비어있으면 오늘 날짜로 처리
        Double rating,
        String oneLineNote,
        String emotion,     // 나의 단어 - 감정
        String mood,        // 나의 단어 - 분위기
        String genre,       // 나의 단어 - 장르
        String readAmount   // ALL(전체 읽었어요) / PARTIAL(일부 읽었어요)
) {
}
