package com.booktalk.domain.auth;

import com.booktalk.domain.auth.client.OAuthClient;
import com.booktalk.domain.auth.dto.OAuthLoginRequest;
import com.booktalk.domain.auth.dto.RefreshTokenRequest;
import com.booktalk.domain.auth.dto.TokenResponse;
import com.booktalk.domain.auth.dto.UserResponse;
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

    public TokenResponse refresh(RefreshTokenRequest request) {
        Long userId = jwtTokenProvider.parseUserIdFromRefreshToken(request.refreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 사용자입니다."));

        return issueTokens(user);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        return new TokenResponse(accessToken, refreshToken, UserResponse.from(user));
    }

    private User findOrCreateUser(OAuthUserInfo info) {
        return userRepository.findByOauthProviderAndProviderId(info.provider().name(), info.providerId())
                .map(existing -> {
                    existing.updateProfile(info.nickname(), info.profileImageUrl());
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
