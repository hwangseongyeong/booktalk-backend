package com.booktalk.domain.admin;

import com.booktalk.domain.admin.dto.AdminMetricsResponse;
import com.booktalk.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 어드민 대시보드 지표. 접근 인가는 SecurityConfig(/api/v1/admin/** → ADMIN 역할)에서 처리한다.
 */
@RestController
@RequestMapping("/api/v1/admin/metrics")
@RequiredArgsConstructor
public class AdminMetricsController {

    private final AdminMetricsService adminMetricsService;

    @GetMapping
    public ApiResponse<AdminMetricsResponse> metrics() {
        return ApiResponse.success(adminMetricsService.getMetrics());
    }
}
