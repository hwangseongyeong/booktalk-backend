package com.booktalk.domain.readingrecord;

import com.booktalk.domain.readingrecord.dto.ReadingRecordCompleteRequest;
import com.booktalk.domain.readingrecord.dto.ReadingRecordResponse;
import com.booktalk.domain.readingrecord.dto.ReadingRecordStartRequest;
import com.booktalk.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reading-records")
@RequiredArgsConstructor
public class ReadingRecordController {

    private final ReadingRecordService readingRecordService;

    /** 책 읽기 시작(등록) */
    @PostMapping
    public ApiResponse<ReadingRecordResponse> start(@Valid @RequestBody ReadingRecordStartRequest request) {
        return ApiResponse.success(readingRecordService.start(request));
    }

    /** 완독 처리(별점/한줄메모 포함) */
    @PatchMapping("/{id}/complete")
    public ApiResponse<ReadingRecordResponse> complete(
            @PathVariable Long id,
            @RequestBody ReadingRecordCompleteRequest request
    ) {
        return ApiResponse.success(readingRecordService.complete(id, request));
    }

    /** 내 독서 기록 목록. status=READING|COMPLETED 로 필터링 가능 */
    @GetMapping
    public ApiResponse<List<ReadingRecordResponse>> getMyRecords(
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(readingRecordService.getMyRecords(status));
    }
}
