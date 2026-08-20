package com.booktalk.domain.auth.client;

import com.booktalk.domain.auth.OAuthProvider;
import com.booktalk.domain.auth.OAuthUserInfo;
import com.booktalk.domain.auth.client.property.KakaoProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private final KakaoProperties properties;
    private final RestClient restClient = RestClient.create();

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String code, String redirectUri, String state) {
        String accessToken = requestAccessToken(code, redirectUri);
        return requestUserInfo(accessToken);
    }

    private String requestAccessToken(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.clientId());
        // 카카오 개발자 콘솔에서 "Client Secret 사용"을 켠 경우에만 필요
        if (properties.clientSecret() != null && !properties.clientSecret().isBlank()) {
            form.add("client_secret", properties.clientSecret());
        }
        form.add("redirect_uri", redirectUri != null ? redirectUri : properties.redirectUri());
        form.add("code", code);

        KakaoTokenResponse response = restClient.post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    log.warn("카카오 토큰 발급 실패 (status={}): {}", res.getStatusCode(), body);
                    throw new IllegalStateException("카카오 토큰 발급에 실패했습니다: " + body);
                })
                .body(KakaoTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("카카오 토큰 발급에 실패했습니다.");
        }
        return response.accessToken();
    }

    private OAuthUserInfo requestUserInfo(String accessToken) {
        KakaoUserResponse response = restClient.get()
                .uri(USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    log.warn("카카오 사용자 정보 조회 실패 (status={}): {}", res.getStatusCode(), body);
                    throw new IllegalStateException("카카오 사용자 정보 조회에 실패했습니다: " + body);
                })
                .body(KakaoUserResponse.class);

        if (response == null) {
            throw new IllegalStateException("카카오 사용자 정보 조회에 실패했습니다.");
        }

        KakaoAccount account = response.kakaoAccount();
        String email = account != null ? account.email() : null;
        String nickname = (account != null && account.profile() != null)
                ? account.profile().nickname()
                : "카카오 사용자";
        String profileImageUrl = (account != null && account.profile() != null)
                ? account.profile().profileImageUrl()
                : null;

        return new OAuthUserInfo(OAuthProvider.KAKAO, String.valueOf(response.id()), email, nickname, profileImageUrl);
    }

    private record KakaoTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record KakaoUserResponse(Long id, @JsonProperty("kakao_account") KakaoAccount kakaoAccount) {
    }

    private record KakaoAccount(String email, KakaoProfile profile) {
    }

    private record KakaoProfile(String nickname, @JsonProperty("profile_image_url") String profileImageUrl) {
    }
}
