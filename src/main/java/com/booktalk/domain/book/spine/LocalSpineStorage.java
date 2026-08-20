package com.booktalk.domain.book.spine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * R2/AWS 계정 만들기 전 임시로 쓰는 로컬 파일 저장 구현.
 * storage.mode가 설정 안 돼 있거나 "local"이면 이 구현체가 활성화된다(기본값).
 * `storage.local.upload-dir` 아래 spines/{bookId}.svg 로 저장하고,
 * LocalUploadResourceConfig가 그 디렉터리를 /uploads/** 정적 경로로 서빙한다.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "storage", name = "mode", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class LocalSpineStorage implements SpineStorage {

    private final LocalStorageProperties properties;

    @Override
    public String upload(Long bookId, byte[] svgContent) {
        try {
            Path dir = Path.of(properties.uploadDir(), "spines");
            Files.createDirectories(dir);

            Path file = dir.resolve(bookId + ".svg");
            Files.write(file, svgContent);

            String base = properties.publicBaseUrl().endsWith("/")
                    ? properties.publicBaseUrl().substring(0, properties.publicBaseUrl().length() - 1)
                    : properties.publicBaseUrl();
            return base + "/spines/" + bookId + ".svg";
        } catch (IOException e) {
            log.warn("로컬 책등 이미지 저장 실패 (bookId={}): {}", bookId, e.getMessage());
            return null;
        }
    }
}
