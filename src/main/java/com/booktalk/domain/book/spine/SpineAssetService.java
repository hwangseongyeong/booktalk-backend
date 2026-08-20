package com.booktalk.domain.book.spine;

import com.booktalk.domain.book.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

/**
 * 책 등록 시 1회 호출되어 책등 이미지를 만들고 Book 엔티티에 캐싱한다.
 *
 * 실패 허용 정책: 표지 조회/색상 추출/R2 업로드 중 어느 하나라도 실패해도 책 등록 자체는 막지 않는다.
 * - 색상 추출 실패 → FallbackPalette로 대체 (항상 primaryColor/accentColor는 채워짐)
 * - R2 미설정/업로드 실패 → spineImageUrl은 null로 남고, 프런트는 primaryColor 기반 색상 블록을 보여준다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpineAssetService {

    private final R2Properties r2Properties;
    private final R2Client r2Client;
    private final RestClient restClient = RestClient.create();

    public void generateAndAttach(Book book) {
        ImageColorExtractor.ExtractedColors colors = extractColors(book);
        String spineImageUrl = tryUploadSpineSvg(book, colors);

        book.updateSpineAssets(spineImageUrl, colors.primaryColor(), colors.accentColor());
    }

    private ImageColorExtractor.ExtractedColors extractColors(Book book) {
        if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isBlank()) {
            try {
                byte[] imageBytes = restClient.get()
                        .uri(book.getCoverImageUrl())
                        .retrieve()
                        .body(byte[].class);

                if (imageBytes != null && imageBytes.length > 0) {
                    return ImageColorExtractor.extract(imageBytes);
                }
            } catch (Exception e) {
                log.warn("표지 이미지 색상 추출 실패 (bookId={}, coverImageUrl={}): {}",
                        book.getId(), book.getCoverImageUrl(), e.getMessage());
            }
        }
        return FallbackPalette.pick(book.getTitle());
    }

    private String tryUploadSpineSvg(Book book, ImageColorExtractor.ExtractedColors colors) {
        if (!r2Properties.isConfigured()) {
            return null;
        }

        try {
            String svg = SpineSvgBuilder.build(book.getTitle(), colors.primaryColor(), colors.accentColor());
            String key = "spines/" + book.getId() + ".svg";
            return r2Client.upload(key, svg.getBytes(StandardCharsets.UTF_8), "image/svg+xml");
        } catch (Exception e) {
            log.warn("책등 이미지 업로드 실패 (bookId={}): {}", book.getId(), e.getMessage());
            return null;
        }
    }
}
