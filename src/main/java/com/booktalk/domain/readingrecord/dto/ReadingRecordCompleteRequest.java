package com.booktalk.domain.readingrecord.dto;

import java.time.LocalDate;
import java.util.List;

public record ReadingRecordCompleteRequest(
        LocalDate endDate, // 비어있으면 오늘 날짜로 처리
        Double rating,
        String oneLineNote,
        List<String> emotions,  // 나의 단어 - 감정(최대 3)
        List<String> moods,     // 나의 단어 - 분위기(최대 3)
        List<String> genres,    // 나의 단어 - 장르(최대 3)
        String readAmount       // ALL(전체 읽었어요) / PARTIAL(일부 읽었어요)
) {
}
