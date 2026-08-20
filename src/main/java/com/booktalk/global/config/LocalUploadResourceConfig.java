package com.booktalk.global.config;

import com.booktalk.domain.book.spine.LocalStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * storage.mode=local(기본값)일 때 저장된 책등 SVG 파일을 /uploads/** 로 그대로 서빙한다.
 * R2로 전환(storage.mode=r2)하면 이 핸들러는 그냥 안 쓰이게 된다(등록 상태로 둬도 무해).
 */
@Configuration
@RequiredArgsConstructor
public class LocalUploadResourceConfig implements WebMvcConfigurer {

    private final LocalStorageProperties localStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = localStorageProperties.uploadDir();
        String location = "file:" + (uploadDir.endsWith("/") ? uploadDir : uploadDir + "/");
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
