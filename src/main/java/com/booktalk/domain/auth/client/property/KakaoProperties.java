package com.booktalk.domain.auth.client.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth2.kakao")
public record KakaoProperties(String clientId, String clientSecret, String redirectUri) {
}
