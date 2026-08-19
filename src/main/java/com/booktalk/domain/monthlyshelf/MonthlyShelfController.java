package com.booktalk.domain.monthlyshelf;

import com.booktalk.domain.monthlyshelf.dto.MonthlyShelfResponse;
import com.booktalk.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shelves")
@RequiredArgsConstructor
public class MonthlyShelfController {

    private final MonthlyShelfService monthlyShelfService;

    /** 월별 서재 조회. yearMonth 미지정 시 이번 달. 예: /api/v1/shelves/monthly?yearMonth=2026-07 */
    @GetMapping("/monthly")
    public ApiResponse<MonthlyShelfResponse> getMonthlyShelf(
            @RequestParam(required = false) String yearMonth
    ) {
        return ApiResponse.success(monthlyShelfService.getMonthlyShelf(yearMonth));
    }
}
