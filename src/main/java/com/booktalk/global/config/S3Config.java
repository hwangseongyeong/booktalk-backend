package com.booktalk.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * S3 저장소 클라이언트 설정. {@code app.upload.storage=s3}일 때만 S3Client 빈을 만든다.
 * (로컬 디스크 저장 모드에서는 이 빈이 없어 AWS SDK가 전혀 초기화되지 않는다.)
 *
 * 자격증명은 {@code app.s3.access-key}/{@code secret-key}가 있으면 그 값을 쓰고,
 * 비어 있으면 기본 자격증명 체인(EC2 IAM 역할, 환경변수, ~/.aws 등)을 사용한다.
 * 운영에서는 IAM 역할 사용을 권장하고, 로컬 개발에서만 키를 주입하는 것을 권장한다.
 */
@Configuration
@ConditionalOnProperty(name = "app.upload.storage", havingValue = "s3")
public class S3Config {

    @Bean
    public S3Client s3Client(
            @Value("${app.s3.region:ap-northeast-2}") String region,
            @Value("${app.s3.access-key:}") String accessKey,
            @Value("${app.s3.secret-key:}") String secretKey) {

        var builder = S3Client.builder().region(Region.of(region));

        if (accessKey != null && !accessKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
