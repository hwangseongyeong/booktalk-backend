package com.booktalk.domain.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * 이미지 파일을 저장하고 다시 읽어오는 서비스.
 *
 * 저장소는 {@code app.upload.storage}로 선택한다.
 * - {@code local}(기본): 로컬 디스크 {@code app.upload.dir}/images 아래 저장.
 * - {@code s3}: {@code app.s3.bucket} 버킷의 {@code images/} 접두어 아래 저장.
 *
 * 어느 모드든 서빙 경로(반환 URL)는 {@code GET /api/v1/uploads/images/{파일명}}으로 동일하다.
 * 그래서 저장소를 바꿔도 DB에 저장된 기존 URL은 그대로 유효하다.
 * S3 모드에서 읽을 때 S3에 없으면 레거시(예전 로컬 저장분)를 로컬 디스크에서 fallback으로 읽는다.
 *
 * 허용 포맷은 jpg/jpeg/png/webp/gif, 크기 상한 10MB.
 */
@Slf4j
@Service
public class UploadService {

    /** content-type → 확장자. 허용 목록이자 서빙 시 MediaType 매핑의 근거. */
    private static final Map<String, String> ALLOWED = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10MB
    private static final String S3_PREFIX = "images/";
    // 저장 시 날짜 폴더(yyyyMMdd)로 분할한다. 폴더 기준 날짜는 한국 시간.
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final Path imageDir;
    private final boolean useS3;
    private final String bucket;
    private final S3Client s3; // S3 모드에서만 존재(null 가능)
    /** CloudFront(또는 S3 공개) 베이스 URL. 설정되면 새 업로드는 이 URL로 직접 서빙(백엔드 프록시 우회). */
    private final String publicBaseUrl;

    public UploadService(
            @Value("${app.upload.dir:uploads}") String uploadDir,
            @Value("${app.upload.storage:local}") String storage,
            @Value("${app.s3.bucket:}") String bucket,
            @Value("${app.s3.public-base-url:}") String publicBaseUrl,
            ObjectProvider<S3Client> s3Provider) {
        this.imageDir = Paths.get(uploadDir, "images").toAbsolutePath().normalize();
        this.useS3 = "s3".equalsIgnoreCase(storage);
        this.bucket = bucket;
        this.s3 = s3Provider.getIfAvailable();
        // 끝 슬래시 제거(뒤에서 "/images/..."를 붙이므로).
        this.publicBaseUrl = (publicBaseUrl == null || publicBaseUrl.isBlank())
                ? null : publicBaseUrl.replaceAll("/+$", "");
        if (useS3) {
            if (this.s3 == null || bucket == null || bucket.isBlank()) {
                throw new IllegalStateException(
                        "app.upload.storage=s3 이지만 S3Client 또는 app.s3.bucket 설정이 없습니다.");
            }
            log.info("이미지 저장소: S3 (bucket={}, publicBaseUrl={})",
                    bucket, this.publicBaseUrl != null ? this.publicBaseUrl : "(없음·백엔드 프록시 서빙)");
        } else {
            log.info("이미지 저장소: 로컬 디스크 ({})", imageDir);
        }
    }

    /**
     * 업로드된 이미지를 저장하고, 프론트에서 쓸 상대 URL을 반환한다.
     * 파일명은 UUID로 새로 부여해 원본 파일명에 의한 충돌/경로 조작을 원천 차단한다.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("파일이 너무 큽니다. 최대 10MB까지 업로드할 수 있습니다.");
        }
        String ext = ALLOWED.get(file.getContentType());
        if (ext == null) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, png, webp, gif만 가능)");
        }

        // 날짜 폴더(yyyyMMdd) 아래에 UUID 파일명으로 저장한다. 예: 20261028/abc.jpg
        String dateDir = LocalDate.now(ZONE).format(DATE_DIR);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relKey = dateDir + "/" + filename;
        try {
            if (useS3) {
                s3.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(S3_PREFIX + relKey)
                                .contentType(file.getContentType())
                                // 파일명이 UUID라 내용 불변 → S3 객체에 장기 immutable 캐시 헤더를 심는다.
                                // CloudFront가 이 헤더를 뷰어로 전달해 클라이언트 로컬 캐시가 적용된다.
                                .cacheControl("public, max-age=31536000, immutable")
                                .build(),
                        RequestBody.fromBytes(file.getBytes()));
            } else {
                Path target = imageDir.resolve(relKey).normalize();
                // resolve 조합의 비정상 경로 방어
                if (!target.startsWith(imageDir)) {
                    throw new IllegalArgumentException("잘못된 파일 경로입니다.");
                }
                Files.createDirectories(target.getParent());
                file.transferTo(target);
            }
        } catch (IOException e) {
            log.error("이미지 저장 실패: {}", e.getMessage(), e);
            throw new UncheckedIOException("이미지 저장에 실패했습니다.", e);
        }
        // CloudFront 등 공개 베이스 URL이 설정돼 있으면 CDN 직접 서빙 URL을 반환(백엔드/S3 egress 우회).
        // 없으면 기존처럼 백엔드 프록시 경로를 반환한다.
        if (publicBaseUrl != null) {
            return publicBaseUrl + "/" + S3_PREFIX + relKey;
        }
        return "/api/v1/uploads/images/" + relKey;
    }

    /**
     * 저장된 이미지 바이트를 읽어온다(백엔드 프록시 경로용). 경로 조작(../ 등)은 거부한다.
     * S3 모드에서는 S3에서 직접 읽는다(로컬 캐시 없음). 정적 서빙은 CloudFront가 담당하며,
     * 이 경로는 공개 베이스 URL 미설정 시의 폴백 용도다. 로컬 저장 모드에서는 로컬 디스크에서 읽는다.
     */
    public byte[] read(String key) {
        validateFilename(key);
        if (useS3) {
            try {
                ResponseBytes<GetObjectResponse> obj = s3.getObjectAsBytes(
                        GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(S3_PREFIX + key)
                                .build());
                return obj.asByteArray();
            } catch (NoSuchKeyException e) {
                throw new IllegalArgumentException("이미지를 찾을 수 없습니다.");
            }
        }
        if (!localReadable(key)) {
            throw new IllegalArgumentException("이미지를 찾을 수 없습니다.");
        }
        return readLocal(key);
    }

    /** 파일명으로 content-type을 추론한다(서빙용). 알 수 없으면 octet-stream. */
    public MediaType contentTypeOf(String filename) {
        int dot = filename.lastIndexOf('.');
        String ext = dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
        return switch (ext) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    // ---------- 로컬 디스크 입출력 ----------

    private boolean localReadable(String filename) {
        Path path = resolveSafe(filename);
        return Files.exists(path) && Files.isReadable(path);
    }

    private byte[] readLocal(String filename) {
        try {
            return Files.readAllBytes(resolveSafe(filename));
        } catch (IOException e) {
            throw new UncheckedIOException("이미지를 읽을 수 없습니다.", e);
        }
    }

    // 키는 날짜 폴더를 포함할 수 있어 슬래시를 허용하되, 경로 탈출(.. / 절대경로)은 거부한다.
    private void validateFilename(String key) {
        if (key == null || key.isBlank()
                || !key.matches("[A-Za-z0-9._/-]+")
                || key.contains("..")
                || key.startsWith("/")) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
    }

    /** 파일명을 검증하고 이미지 디렉토리 내부 경로로만 해석한다(경로 탈출 방지). */
    private Path resolveSafe(String filename) {
        validateFilename(filename);
        Path path = imageDir.resolve(filename).normalize();
        if (!path.startsWith(imageDir)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
        return path;
    }
}
