package com.booktalk.global.config;

import com.booktalk.domain.auth.client.property.FacebookProperties;
import com.booktalk.domain.auth.client.property.GoogleProperties;
import com.booktalk.domain.auth.client.property.KakaoProperties;
import com.booktalk.domain.auth.client.property.NaverProperties;
import com.booktalk.domain.book.spine.LocalStorageProperties;
import com.booktalk.domain.book.spine.R2Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        KakaoProperties.class,
        NaverProperties.class,
        GoogleProperties.class,
        FacebookProperties.class,
        R2Properties.class,
        LocalStorageProperties.class
})
public class ExternalPropertiesConfig {
}
