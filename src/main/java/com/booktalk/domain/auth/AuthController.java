package com.booktalk.domain.auth;

import com.booktalk.domain.auth.dto.OAuthLoginRequest;
import com.booktalk.domain.auth.dto.RefreshTokenRequest;
import com.booktalk.domain.auth.dto.TokenResponse;
import com.booktalk.domain.auth.dto.UserResponse;
import com.booktalk.global.common.ApiResponse;
import com.booktalk.global.security.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUserResolver currentUserResolver;

    /**
     * 소셜 로그인. provider: kakao | naver | google | facebook
     * 프런트에서 각 제공자 인가 화면으로 리다이렉트 후 받은 code(및 네이버는 state)를 그대로 전달.
     */
    @PostMapping("/{provider}/login")
    public ApiResponse<TokenResponse> login(
            @PathVariable String provider,
            @Valid @RequestBody OAuthLoginRequest request
    ) {
        return ApiResponse.success(authService.login(parseProvider(provider), request));
    }

    /** access token 만료 시 refresh token으로 재발급 */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    /** 현재 로그인한 사용자 정보 */
    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.success(UserResponse.from(currentUserResolver.getCurrentUser()));
    }

    private OAuthProvider parseProvider(String provider) {
        try {
            return OAuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 로그인 방식입니다: " + provider);
        }
    }
}
