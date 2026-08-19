package com.booktalk.domain.auth.client;

import com.booktalk.domain.auth.OAuthProvider;
import com.booktalk.domain.auth.OAuthUserInfo;
import com.booktalk.domain.auth.client.property.FacebookProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class FacebookOAuthClient implements OAuthClient {

    private static final String TOKEN_URI = "https://graph.facebook.com/v19.0/oauth/access_token";
    private static final String USER_INFO_URI = "https://graph.facebook.com/me";

    private final FacebookProperties properties;
    private final RestClient restClient = RestClient.create();

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.FACEBOOK;
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String code, String redirectUri, String state) {
        String accessToken = requestAccessToken(code, redirectUri);
        return requestUserInfo(accessToken);
    }

    private String requestAccessToken(String code, String redirectUri) {
        String uri = UriComponentsBuilder.fromHttpUrl(TOKEN_URI)
                .queryParam("client_id", properties.clientId())
                .queryParam("client_secret", properties.clientSecret())
                .queryParam("redirect_uri", redirectUri != null ? redirectUri : properties.redirectUri())
                .queryParam("code", code)
                .build()
                .toUriString();

        FacebookTokenResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(FacebookTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("페이스북 토큰 발급에 실패했습니다.");
        }
        return response.accessToken();
    }

    private OAuthUserInfo requestUserInfo(String accessToken) {
        String uri = UriComponentsBuilder.fromHttpUrl(USER_INFO_URI)
                .queryParam("fields", "id,name,email,picture")
                .queryParam("access_token", accessToken)
                .build()
                .toUriString();

        FacebookUserResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(FacebookUserResponse.class);

        if (response == null) {
            throw new IllegalStateException("페이스북 사용자 정보 조회에 실패했습니다.");
        }

        String profileImageUrl = (response.picture() != null && response.picture().data() != null)
                ? response.picture().data().url()
                : null;

        return new OAuthUserInfo(OAuthProvider.FACEBOOK, response.id(), response.email(), response.name(), profileImageUrl);
    }

    private record FacebookTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record FacebookUserResponse(String id, String name, String email, FacebookPicture picture) {
    }

    private record FacebookPicture(FacebookPictureData data) {
    }

    private record FacebookPictureData(String url) {
    }
}
