package com.booktalk.domain.book.spine;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS S3 접속 정보. storage.mode=s3 일 때 사용된다.
 * region/bucket/publicBaseUrl이 비어있으면 업로드를 건너뛰고 색상만 채운다.
 *
 * accessKey/secretKey는 로컬 개발 등에서만 사용하고, EC2/ECS에 배포할 때는
 * 비워두는 것을 권장한다. 비어있으면 AWS SDK 기본 자격증명 체인(IAM Role 등)을 사용한다.
 *
 * publicBaseUrl: 버킷을 CloudFront로 서빙할 때는 CloudFront 도메인,
 * S3 직접 공개 시에는 https://{bucket}.s3.{region}.amazonaws.com 형태.
 */
@ConfigurationProperties(prefix = "s3")
public record S3Properties(
        String region,
        String bucket,
        String accessKey,
        String secretKey,
        String publicBaseUrl
) {
    public boolean isConfigured() {
        return region != null && !region.isBlank()
                && bucket != null && !bucket.isBlank()
                && publicBaseUrl != null && !publicBaseUrl.isBlank();
    }

    /** 정적 키가 모두 채워져 있으면 true. 아니면 기본 자격증명 체인(IAM Role 등)을 사용한다. */
    public boolean hasStaticCredentials() {
        return accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank();
    }
}
