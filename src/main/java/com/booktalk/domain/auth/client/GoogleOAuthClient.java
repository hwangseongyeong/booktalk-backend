package com.booktalk.domain.auth.client;

import com.booktalk.domain.auth.OAuthProvider;
import com.booktalk.domain.auth.OAuthUserInfo;
import com.booktalk.domain.auth.client.property.GoogleProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GoogleOAuthClient implements OAuthClient {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final GoogleProperties properties;
    private final RestClient restClient = RestClient.create();

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.GOOGLE;
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
        form.add("client_secret", properties.clientSecret());
        form.add("redirect_uri", redirectUri != null ? redirectUri : properties.redirectUri());
        form.add("code", code);

        GoogleTokenResponse response = restClient.post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("구글 토큰 발급에 실패했습니다.");
        }
        return response.accessToken();
    }

    private OAuthUserInfo requestUserInfo(String accessToken) {
        GoogleUserResponse response = restClient.get()
                .uri(USER_INFO_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserResponse.class);

        if (response == null) {
            throw new IllegalStateException("구글 사용자 정보 조회에 실패했습니다.");
        }

        return new OAuthUserInfo(OAuthProvider.GOOGLE, response.sub(), response.email(), response.name(), response.picture());
    }

    private record GoogleTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record GoogleUserResponse(String sub, String email, String name, String picture) {
    }
}
