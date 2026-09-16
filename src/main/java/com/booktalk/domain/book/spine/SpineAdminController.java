package com.booktalk.domain.book.spine;

import com.booktalk.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 책등 유지보수용 관리자 엔드포인트.
 *
 * 인증은 JWT가 아니라 X-Admin-Token 헤더(=admin.api-token 설정값)로 처리한다.
 * admin.api-token이 비어 있으면(기본값) 항상 403이라 실수로 노출돼도 동작하지 않는다.
 * SecurityConfig에서 /api/v1/admin/** 는 permitAll로 열려 있고, 실제 인가는 여기서 한다.
 */
@RestController
@RequestMapping("/api/v1/admin/spines")
@RequiredArgsConstructor
public class SpineAdminController {

    private final SpineRegenerationService spineRegenerationService;

    @Value("${admin.api-token:}")
    private String adminApiToken;

    /** 기존 모든 책의 책등을 현재 로직으로 강제 재생성한다. */
    @PostMapping("/regenerate")
    public ResponseEntity<?> regenerateAll(
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (adminApiToken == null || adminApiToken.isBlank() || !adminApiToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("유효한 관리자 토큰이 필요합니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(spineRegenerationService.regenerateAll()));
    }
}
