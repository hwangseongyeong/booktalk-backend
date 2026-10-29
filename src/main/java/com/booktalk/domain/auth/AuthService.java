package com.booktalk.domain.auth;

import com.booktalk.domain.auth.client.KakaoOAuthClient;
import com.booktalk.domain.auth.client.OAuthClient;
import com.booktalk.domain.auth.dto.OAuthLoginRequest;
import com.booktalk.domain.auth.dto.RefreshTokenRequest;
import com.booktalk.domain.auth.dto.TokenResponse;
import com.booktalk.domain.user.dto.UserProfileResponse;
import com.booktalk.domain.user.User;
import com.booktalk.domain.user.UserRepository;
import com.booktalk.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final List<OAuthClient> oauthClients;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private Map<OAuthProvider, OAuthClient> clientsByProvider;

    @Transactional
    public TokenResponse login(OAuthProvider provider, OAuthLoginRequest request) {
        OAuthClient client = resolveClient(provider);

        OAuthUserInfo userInfo = client.fetchUserInfo(request.code(), request.redirectUri(), request.state());
        User user = findOrCreateUser(userInfo);

        return issueTokens(user);
    }

    /** 네이티브 카카오 SDK 로그인: 앱이 받은 카카오 access token으로 사용자 조회 후 JWT 발급. */
    @Transactional
    public TokenResponse loginWithKakaoAccessToken(String kakaoAccessToken) {
        OAuthUserInfo userInfo = kakaoOAuthClient.fetchUserInfoByAccessToken(kakaoAccessToken);
        User user = findOrCreateUser(userInfo);
        return issueTokens(user);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        Long userId = jwtTokenProvider.parseUserIdFromRefreshToken(request.refreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 사용자입니다."));

        return issueTokens(user);
    }

    private TokenResponse issueTokens(User user) {
        if (user.isSuspended()) {
            throw new IllegalStateException("정지된 계정입니다. 운영자에게 문의해주세요.");
        }
        // Access 토큰에 role을 담아 어드민 가드의 근거로 쓴다. 역할 변경은 다음 로그인/리프레시 때 반영된다.
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        return new TokenResponse(accessToken, refreshToken, UserProfileResponse.from(user));
    }

    private User findOrCreateUser(OAuthUserInfo info) {
        return userRepository.findByOauthProviderAndProviderId(info.provider().name(), info.providerId())
                .map(existing -> {
                    if (!existing.isOnboardingCompleted()) {
                        // 온보딩 전에는 소셜 제공자의 최신 프로필로 갱신한다.
                        existing.syncOAuthProfile(info.nickname(), info.profileImageUrl());
                    } else {
                        // 온보딩을 마친 사용자는 본인이 정한 값을 유지하되, DB에 없던 항목만 다음 로그인 때 채운다.
                        existing.fillMissingOAuthProfile(info.nickname(), info.profileImageUrl());
                    }
                    return existing;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(info.email())
                        .nickname(info.nickname() != null ? info.nickname() : "BookTalk 사용자")
                        .profileImageUrl(info.profileImageUrl())
                        .oauthProvider(info.provider().name())
                        .providerId(info.providerId())
                        .build()));
    }

    private OAuthClient resolveClient(OAuthProvider provider) {
        if (clientsByProvider == null) {
            clientsByProvider = oauthClients.stream()
                    .collect(Collectors.toMap(OAuthClient::provider, c -> c));
        }
        OAuthClient client = clientsByProvider.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("지원하지 않는 로그인 방식입니다: " + provider);
        }
        return client;
    }
}
