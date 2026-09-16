package com.booktalk.domain.book.spine;

import com.booktalk.domain.book.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
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

    // 표지 이미지 URL이 리다이렉트(301/302)하는 경우도 있어 리다이렉트를 따라가도록 설정한다.
    private final RestClient restClient = RestClient.builder()
            .requestFactory(new JdkClientHttpRequestFactory(
                    HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()))
            .build();

    public void generateAndAttach(Book book) {
        byte[] coverBytes = fetchCoverBytes(book);
        ImageColorExtractor.ExtractedColors colors = extractColors(coverBytes, book.getTitle());

        String svg = buildSvg(book.getTitle(), coverBytes, colors);
        String spineImageUrl = spineStorage.upload(book.getId(), svg.getBytes(StandardCharsets.UTF_8));

        book.updateSpineAssets(spineImageUrl, colors.primaryColor(), colors.accentColor());
    }

    /** 표지 이미지를 1회 내려받는다. 색상 추출과 책등 배경에 함께 사용. 실패 시 null. */
    private byte[] fetchCoverBytes(Book book) {
        if (book.getCoverImageUrl() == null || book.getCoverImageUrl().isBlank()) {
            return null;
        }
        try {
            byte[] bytes = restClient.get()
                    .uri(book.getCoverImageUrl())
                    .retrieve()
                    .body(byte[].class);
            return (bytes != null && bytes.length > 0) ? bytes : null;
        } catch (Exception e) {
            log.warn("표지 이미지 조회 실패 (bookId={}, coverImageUrl={}): {}",
                    book.getId(), book.getCoverImageUrl(), e.getMessage());
            return null;
        }
    }

    private ImageColorExtractor.ExtractedColors extractColors(byte[] coverBytes, String title) {
        if (coverBytes != null) {
            try {
                return ImageColorExtractor.extract(coverBytes);
            } catch (Exception e) {
                log.warn("표지 색상 추출 실패, 폴백 팔레트 사용: {}", e.getMessage());
            }
        }
        return FallbackPalette.pick(title);
    }

    /** 표지가 있으면 표지 세로 슬라이스 책등, 없거나 실패하면 색상 기반 책등으로 폴백. */
    private String buildSvg(String title, byte[] coverBytes, ImageColorExtractor.ExtractedColors colors) {
        if (coverBytes != null) {
            try {
                String coverDataUri = CoverImageEncoder.toSpineSliceDataUri(coverBytes);
                return SpineSvgBuilder.buildFromCover(title, coverDataUri);
            } catch (Exception e) {
                log.warn("표지 기반 책등 생성 실패, 색상 기반으로 폴백: {}", e.getMessage());
            }
        }
        return SpineSvgBuilder.buildFromColors(title, colors.primaryColor(), colors.accentColor());
    }
}
