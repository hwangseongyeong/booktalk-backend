package com.booktalk.domain.book.spine;

import com.booktalk.domain.book.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

/**
 * 책 등록 시 1회 호출되어 책등 이미지를 만들고 Book 엔티티에 캐싱한다.
 * 실제 저장 방식(로컬 디스크 / R2)은 SpineStorage 구현체가 storage.mode 설정에 따라 결정한다.
 *
 * 실패 허용 정책: 표지 조회/색상 추출/업로드 중 어느 하나라도 실패해도 책 등록 자체는 막지 않는다.
 * - 색상 추출 실패 → FallbackPalette로 대체 (항상 primaryColor/accentColor는 채워짐)
 * - 업로드 실패 → spineImageUrl은 null로 남고, 프런트는 primaryColor 기반 색상 블록을 보여준다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpineAssetService {

    private final SpineStorage spineStorage;
    private final RestClient restClient = RestClient.create();

    public void generateAndAttach(Book book) {
        ImageColorExtractor.ExtractedColors colors = extractColors(book);

        String svg = SpineSvgBuilder.build(book.getTitle(), colors.primaryColor(), colors.accentColor());
        String spineImageUrl = spineStorage.upload(book.getId(), svg.getBytes(StandardCharsets.UTF_8));

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
}
