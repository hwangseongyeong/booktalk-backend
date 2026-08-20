package com.booktalk.domain.book.spine;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 로컬 디스크에 책등 이미지를 저장할 때 사용하는 설정.
 * R2/AWS 계정을 아직 안 만들었을 때 storage.mode=local(기본값)로 두면 이걸 사용한다.
 */
@ConfigurationProperties(prefix = "storage.local")
public record LocalStorageProperties(String uploadDir, String publicBaseUrl) {

    public LocalStorageProperties {
        if (uploadDir == null || uploadDir.isBlank()) {
            uploadDir = "./uploads";
        }
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            publicBaseUrl = "http://localhost:8080/uploads";
        }
    }
}
