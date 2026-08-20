package com.booktalk.domain.book.spine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

/**
 * Cloudflare R2는 S3 호환 API를 제공하므로 AWS SDK의 S3Client를
 * R2 전용 엔드포인트(https://{accountId}.r2.cloudflarestorage.com)로 사용한다.
 * region은 R2에서 의미가 없어 "auto"로 고정.
 */
@Component
@RequiredArgsConstructor
public class R2Client {

    private final R2Properties properties;

    private volatile S3Client client;

    /** 업로드 후 공개 URL을 반환한다. */
    public String upload(String key, byte[] content, String contentType) {
        s3Client().putObject(
                PutObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(content)
        );

        String base = properties.publicBaseUrl().endsWith("/")
                ? properties.publicBaseUrl().substring(0, properties.publicBaseUrl().length() - 1)
                : properties.publicBaseUrl();
        return base + "/" + key;
    }

    private S3Client s3Client() {
        S3Client instance = client;
        if (instance == null) {
            synchronized (this) {
                if (client == null) {
                    client = S3Client.builder()
                            .endpointOverride(URI.create("https://" + properties.accountId() + ".r2.cloudflarestorage.com"))
                            .region(Region.of("auto"))
                            .credentialsProvider(StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
                            .build();
                }
                instance = client;
            }
        }
        return instance;
    }
}
