package com.booktalk.domain.auth.client;

import com.booktalk.domain.auth.OAuthProvider;
import com.booktalk.domain.auth.OAuthUserInfo;
import com.booktalk.domain.auth.client.property.NaverProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class NaverOAuthClient implements OAuthClient {

    private static final String TOKEN_URI = "https://nid.naver.com/oauth2.0/token";
    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";

    private final NaverProperties properties;
    private final RestClient restClient = RestClient.create();

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.NAVER;
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String code, String redirectUri, String state) {
        // 네이버는 redirect_uri 대신 인가 요청 시 사용한 state 값을 그대로 검증한다.
        String accessToken = requestAccessToken(code, state);
        return requestUserInfo(accessToken);
    }

    private String requestAccessToken(String code, String state) {
        String uri = UriComponentsBuilder.fromHttpUrl(TOKEN_URI)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", properties.clientId())
                .queryParam("client_secret", properties.clientSecret())
                .queryParam("code", code)
                .queryParam("state", state != null ? state : "")
                .build()
                .toUriString();

        NaverTokenResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(NaverTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("네이버 토큰 발급에 실패했습니다.");
        }
        return response.accessToken();
    }

    private OAuthUserInfo requestUserInfo(String accessToken) {
        NaverUserResponse response = restClient.get()
                .uri(USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(NaverUserResponse.class);

        if (response == null || response.response() == null) {
            throw new IllegalStateException("네이버 사용자 정보 조회에 실패했습니다.");
        }

        NaverUserInfo info = response.response();
        return new OAuthUserInfo(OAuthProvider.NAVER, info.id(), info.email(), info.nickname(), info.profileImage());
    }

    private record NaverTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record NaverUserResponse(String resultcode, String message, NaverUserInfo response) {
    }

    private record NaverUserInfo(
            String id,
            String email,
            String nickname,
            @JsonProperty("profile_image") String profileImage
    ) {
    }
}
