package com.booktalk.domain.book.spine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * AWS S3에 책등 이미지를 저장하는 구현.
 * storage.mode=s3 로 명시했을 때만 활성화된다.
 *
 * 책등 SVG는 책당 1회 생성 후 불변이므로 장기 캐시(Cache-Control)를 걸어
 * CloudFront/브라우저 캐싱 효율을 높인다.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "storage", name = "mode", havingValue = "s3")
@RequiredArgsConstructor
public class S3SpineStorage implements SpineStorage {

    private static final String CACHE_CONTROL = "public, max-age=31536000, immutable";

    private final S3Properties properties;

    private volatile S3Client client;

    @Override
    public String upload(Long bookId, byte[] svgContent) {
        if (!properties.isConfigured()) {
            log.warn("storage.mode=s3 인데 S3 설정(region/bucket/public-base-url)이 비어있습니다. 업로드를 건너뜁니다.");
            return null;
        }

        try {
            String key = SpineObjectKey.forBook(bookId);
            s3Client().putObject(
                    PutObjectRequest.builder()
                            .bucket(properties.bucket())
                            .key(key)
                            .contentType("image/svg+xml")
                            .cacheControl(CACHE_CONTROL)
                            .build(),
                    RequestBody.fromBytes(svgContent)
            );

            String base = properties.publicBaseUrl().endsWith("/")
                    ? properties.publicBaseUrl().substring(0, properties.publicBaseUrl().length() - 1)
                    : properties.publicBaseUrl();
            return base + "/" + key;
        } catch (Exception e) {
            log.warn("S3 책등 이미지 업로드 실패 (bookId={}): {}", bookId, e.getMessage());
            return null;
        }
    }

    private S3Client s3Client() {
        S3Client instance = client;
        if (instance == null) {
            synchronized (this) {
                if (client == null) {
                    var builder = S3Client.builder()
                            .region(Region.of(properties.region()));
                    // 정적 키가 있으면 사용, 없으면 기본 자격증명 체인(EC2/ECS IAM Role 등)에 위임
                    if (properties.hasStaticCredentials()) {
                        builder.credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())));
                    }
                    client = builder.build();
                }
                instance = client;
            }
        }
        return instance;
    }
}
