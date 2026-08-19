package com.booktalk.domain.auth.client.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth2.google")
public record GoogleProperties(String clientId, String clientSecret, String redirectUri) {
}
