package com.booktalk.domain.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * 이미지 파일을 로컬 디스크에 저장하고 다시 읽어오는 서비스 (MVP 수준).
 *
 * 저장 위치는 {@code app.upload.dir}(기본 "uploads") 아래 {@code images/} 폴더이고,
 * 서빙 경로는 {@code GET /api/v1/uploads/images/{파일명}} 이다. S3 전환 전까지의 로컬 저장소.
 *
 * 허용 포맷은 jpg/jpeg/png/webp/gif로 제한한다. 파일 크기 상한은 멀티파트 설정
 * (spring.servlet.multipart.max-file-size)에서 1차로 막고, 여기서도 방어적으로 검사한다.
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

    private final Path imageDir;

    public UploadService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.imageDir = Paths.get(uploadDir, "images").toAbsolutePath().normalize();
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

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Files.createDirectories(imageDir);
            Path target = imageDir.resolve(filename).normalize();
            // createDirectories/resolve 조합의 비정상 경로 방어
            if (!target.startsWith(imageDir)) {
                throw new IllegalArgumentException("잘못된 파일 경로입니다.");
            }
            file.transferTo(target);
        } catch (IOException e) {
            log.error("이미지 저장 실패: {}", e.getMessage(), e);
            throw new UncheckedIOException("이미지 저장에 실패했습니다.", e);
        }
        return "/api/v1/uploads/images/" + filename;
    }

    /** 저장된 이미지 바이트를 읽어온다. 경로 조작(../ 등)은 거부한다. */
    public byte[] read(String filename) {
        Path path = resolveSafe(filename);
        if (!Files.exists(path) || !Files.isReadable(path)) {
            throw new IllegalArgumentException("이미지를 찾을 수 없습니다.");
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException("이미지를 읽을 수 없습니다.", e);
        }
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

    /** 파일명을 검증하고 이미지 디렉토리 내부 경로로만 해석한다(경로 탈출 방지). */
    private Path resolveSafe(String filename) {
        if (filename == null || !filename.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("잘못된 파일명입니다.");
        }
        Path path = imageDir.resolve(filename).normalize();
        if (!path.startsWith(imageDir)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
        return path;
    }
}
