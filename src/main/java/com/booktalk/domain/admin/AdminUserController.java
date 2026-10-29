package com.booktalk.domain.admin;

import com.booktalk.domain.admin.dto.AdminPage;
import com.booktalk.domain.admin.dto.AdminUserDetail;
import com.booktalk.domain.admin.dto.AdminUserListItem;
import com.booktalk.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 어드민 회원 관리 API. 인가는 SecurityConfig(/api/v1/admin/** → ADMIN)에서 처리.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<AdminPage<AdminUserListItem>> list(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "id"));
        return ApiResponse.success(adminUserService.list(query, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserDetail> detail(@PathVariable Long id) {
        return ApiResponse.success(adminUserService.detail(id));
    }

    @PatchMapping("/{id}/suspend")
    public ApiResponse<Void> suspend(@PathVariable Long id) {
        adminUserService.suspend(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/unsuspend")
    public ApiResponse<Void> unsuspend(@PathVariable Long id) {
        adminUserService.unsuspend(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/grant-admin")
    public ApiResponse<Void> grantAdmin(@PathVariable Long id) {
        adminUserService.grantAdmin(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/revoke-admin")
    public ApiResponse<Void> revokeAdmin(@PathVariable Long id) {
        adminUserService.revokeAdmin(id);
        return ApiResponse.success(null);
    }
}
