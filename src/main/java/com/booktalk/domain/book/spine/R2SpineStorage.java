package com.booktalk.domain.book.spine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Cloudflare R2에 책등 이미지를 저장하는 구현.
 * storage.mode=r2 로 명시했을 때만 활성화된다 (R2 계정/버킷 준비되면 전환).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "storage", name = "mode", havingValue = "r2")
@RequiredArgsConstructor
public class R2SpineStorage implements SpineStorage {

    private final R2Properties properties;
    private final R2Client r2Client;

    @Override
    public String upload(Long bookId, byte[] svgContent) {
        if (!properties.isConfigured()) {
            log.warn("storage.mode=r2 인데 R2 설정(account-id/bucket/public-base-url)이 비어있습니다. 업로드를 건너뜁니다.");
            return null;
        }

        try {
            String key = "spines/" + bookId + ".svg";
            return r2Client.upload(key, svgContent, "image/svg+xml");
        } catch (Exception e) {
            log.warn("R2 책등 이미지 업로드 실패 (bookId={}): {}", bookId, e.getMessage());
            return null;
        }
    }
}
