package com.booktalk.domain.book.spine;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cloudflare R2 접속 정보. R2는 S3 호환 API라 AWS SDK S3Client를 커스텀 엔드포인트로 사용한다.
 * accountId/bucket이 비어있으면 R2 업로드를 건너뛰고 색상만 채운다(로컬 개발 시 R2 없이도 동작).
 *
 * publicBaseUrl: R2 버킷의 "Public access"를 켜면 나오는 https://pub-xxxx.r2.dev 주소,
 * 또는 Cloudflare에 연결한 커스텀 도메인.
 */
@ConfigurationProperties(prefix = "r2")
public record R2Properties(
        String accountId,
        String accessKey,
        String secretKey,
        String bucket,
        String publicBaseUrl
) {
    public boolean isConfigured() {
        return accountId != null && !accountId.isBlank()
                && bucket != null && !bucket.isBlank()
                && publicBaseUrl != null && !publicBaseUrl.isBlank();
    }
}
