package com.booktalk.global.config;

import com.booktalk.domain.auth.client.property.FacebookProperties;
import com.booktalk.domain.auth.client.property.GoogleProperties;
import com.booktalk.domain.auth.client.property.KakaoProperties;
import com.booktalk.domain.auth.client.property.NaverProperties;
import com.booktalk.domain.book.external.KakaoBookProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        KakaoProperties.class,
        NaverProperties.class,
        GoogleProperties.class,
        FacebookProperties.class,
        KakaoBookProperties.class
})
public class ExternalPropertiesConfig {
}
