package com.booktalk.domain.user;

import com.booktalk.domain.user.dto.UserProfileResponse;
import com.booktalk.domain.user.dto.UserUpdateRequest;
import com.booktalk.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 현재 로그인한 사용자 정보 (auth/me와 동일 응답, 리소스 관점의 별칭) */
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMe() {
        return ApiResponse.success(userService.getMyProfile());
    }

    /** 닉네임/프로필 수정 (온보딩 닉네임·프로필 단계 포함) */
    @PatchMapping("/me")
    public ApiResponse<UserProfileResponse> updateMe(@Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success(userService.updateMyProfile(request));
    }

    /** 온보딩 완료 처리 */
    @PostMapping("/me/onboarding/complete")
    public ApiResponse<UserProfileResponse> completeOnboarding() {
        return ApiResponse.success(userService.completeOnboarding());
    }
}
