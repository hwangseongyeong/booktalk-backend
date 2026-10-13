package com.booktalk.domain.book.spine;

import com.booktalk.domain.book.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;

/**
 * 독서 기록 최초 생성 시 1회 호출되어 책등 색상(primary/accent)을 Book 엔티티에 캐싱한다.
 *
 * 책등 SVG는 저장하지 않고 {@code GET /api/v1/books/{id}/spine.svg}에서 실시간 렌더한다
 * ({@link #renderSvg}). SVG는 색 2개로부터 문자열만 조립하므로 재생성 비용이 사실상 없고,
 * 디자인을 바꿔도 배포 즉시 전 도서에 반영된다(사전 생성물 재생성 불필요).
 *
 * 외부 저장소 업로드 경로({@link #uploadSvg}, SpineStorage)는 추후 사전생성/CDN 캐싱에
 * 대비해 그대로 보존하되 현재 파이프라인에서는 호출하지 않는다.
 *
 * 실패 허용 정책: 표지 조회/색상 추출이 실패해도 독서 기록 생성 자체는 막지 않는다.
 * - 색상 추출 실패 → FallbackPalette로 대체 (항상 primaryColor/accentColor는 채워짐)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpineAssetService {

    // 일부 CDN(카카오 등)은 기본 HTTP 클라이언트 User-Agent를 봇으로 보고 403을 준다.
    // 브라우저처럼 보이는 User-Agent를 기본 헤더로 붙인다.
    private static final String BROWSER_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/120.0.0.0 Safari/537.36";

    private final SpineStorage spineStorage;

    // 표지 프록시(Cloudflare Worker) 설정. 비어 있으면 프록시 없이 직접 조회.
    // 카카오 CDN이 서버(AWS) IP를 차단하므로, 설정 시 비-AWS IP인 프록시 경유로 표지를 받는다.
    @Value("${cover-proxy.base-url:}")
    private String coverProxyBaseUrl;

    @Value("${cover-proxy.token:}")
    private String coverProxyToken;

    // 표지 이미지 URL이 리다이렉트(301/302)하는 경우도 있어 리다이렉트를 따라가도록 설정한다.
    private final RestClient restClient = RestClient.builder()
            .requestFactory(new JdkClientHttpRequestFactory(
                    HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()))
            .defaultHeader("User-Agent", BROWSER_USER_AGENT)
            .build();

    public void generateAndAttach(Book book) {
        byte[] coverBytes = fetchCoverBytes(book);
        ImageColorExtractor.ExtractedColors colors = extractColors(coverBytes, book.getTitle());

        // 책등 SVG는 저장하지 않고 /api/v1/books/{id}/spine.svg 에서 실시간 렌더한다.
        // 색만 캐싱해두면 렌더는 사실상 무비용. spineImageUrl은 쓰지 않으므로 null.
        book.updateSpineAssets(null, colors.primaryColor(), colors.accentColor());
    }

    /**
     * Book에 캐싱된 색으로 책등 SVG를 실시간 렌더한다(저장 없음).
     * 아직 색이 없으면(독서 기록 전) FallbackPalette로 제목 기반 색을 쓴다 — 항상 유효한 SVG를 반환.
     */
    public String renderSvg(Book book) {
        String primary = book.getPrimaryColor();
        String accent = book.getAccentColor();
        if (primary == null || accent == null) {
            ImageColorExtractor.ExtractedColors fallback = FallbackPalette.pick(book.getTitle());
            primary = fallback.primaryColor();
            accent = fallback.accentColor();
        }
        return SpineSvgBuilder.buildFromColors(
                book.getTitle(), book.getAuthor(), book.getPublisher(), primary, accent);
    }

    /**
     * (현재 미사용) 책등 SVG를 외부 저장소(SpineStorage)에 업로드하고 공개 URL을 반환한다.
     * 실시간 렌더로 전환하면서 파이프라인에서 제외했으나, 사전생성/CDN 캐싱 도입 시 재사용하려고 보존한다.
     */
    @SuppressWarnings("unused")
    private String uploadSvg(Book book, ImageColorExtractor.ExtractedColors colors) {
        String svg = buildSvg(book, colors);
        return spineStorage.upload(book.getId(), svg.getBytes(StandardCharsets.UTF_8));
    }

    /** 표지 이미지를 1회 내려받는다. 색상 추출과 책등 배경에 함께 사용. 실패 시 null. */
    private byte[] fetchCoverBytes(Book book) {
        if (book.getCoverImageUrl() == null || book.getCoverImageUrl().isBlank()) {
            return null;
        }
        String coverUrl = book.getCoverImageUrl();

        // 1) 프록시(Cloudflare Worker)가 설정돼 있으면 우선 프록시 경유로 조회 (서버 IP 차단 우회)
        if (coverProxyBaseUrl != null && !coverProxyBaseUrl.isBlank()) {
            byte[] viaProxy = tryFetchViaProxy(book, coverUrl);
            if (viaProxy != null) {
                return viaProxy;
            }
        }

        // 2) 직접 조회. 카카오 썸네일 URL이면 fname 원본을 우선 시도하고, 실패 시 원래 URL로 폴백.
        String primaryUrl = resolveOriginalUrl(coverUrl);
        byte[] bytes = tryFetch(book, primaryUrl);
        if (bytes == null && !primaryUrl.equals(coverUrl)) {
            bytes = tryFetch(book, coverUrl);
        }
        return bytes;
    }

    /** Cloudflare Worker 프록시 경유로 표지 이미지를 받는다. 비-AWS IP라 카카오 IP 차단을 우회한다. */
    private byte[] tryFetchViaProxy(Book book, String coverUrl) {
        try {
            String proxyUrl = coverProxyBaseUrl
                    + (coverProxyBaseUrl.contains("?") ? "&" : "?")
                    + "url=" + URLEncoder.encode(coverUrl, StandardCharsets.UTF_8);

            // URI.create로 넘겨 RestClient의 URI 템플릿 재인코딩(이중 인코딩)을 막는다.
            RestClient.RequestHeadersSpec<?> spec = restClient.get().uri(URI.create(proxyUrl));
            if (coverProxyToken != null && !coverProxyToken.isBlank()) {
                spec = spec.header("X-Proxy-Token", coverProxyToken);
            }
            byte[] bytes = spec.retrieve().body(byte[].class);
            return (bytes != null && bytes.length > 0) ? bytes : null;
        } catch (Exception e) {
            log.warn("표지 프록시 조회 실패 (bookId={}): {}", book.getId(), e.getMessage());
            return null;
        }
    }

    private byte[] tryFetch(Book book, String url) {
        try {
            byte[] bytes = restClient.get()
                    .uri(url)
                    .header("Referer", refererOf(url))
                    .retrieve()
                    .body(byte[].class);
            return (bytes != null && bytes.length > 0) ? bytes : null;
        } catch (Exception e) {
            log.warn("표지 이미지 조회 실패 (bookId={}, url={}): {}", book.getId(), url, e.getMessage());
            return null;
        }
    }

    /** 카카오 썸네일 URL이면 fname 파라미터의 원본 이미지 URL을 반환한다. 아니면 원래 URL 그대로. */
    private String resolveOriginalUrl(String coverImageUrl) {
        try {
            URI uri = URI.create(coverImageUrl);
            String query = uri.getQuery();
            if (query != null && query.contains("fname=")) {
                for (String param : query.split("&")) {
                    if (param.startsWith("fname=")) {
                        String fname = java.net.URLDecoder.decode(
                                param.substring("fname=".length()), StandardCharsets.UTF_8);
                        if (fname.startsWith("http")) {
                            return fname;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("원본 표지 URL 추출 실패, 원래 URL 사용: {}", e.getMessage());
        }
        return coverImageUrl;
    }

    /** 표지 URL의 origin(scheme://host)을 Referer로 사용한다. 핫링크 차단 우회에 도움. */
    private String refererOf(String coverImageUrl) {
        try {
            URI uri = URI.create(coverImageUrl);
            return uri.getScheme() + "://" + uri.getHost() + "/";
        } catch (Exception e) {
            return coverImageUrl;
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

    /**
     * 색상 기반으로 책등을 그린다. 저해상도 표지를 세로로 잘라 배경에 깔면 뿌옇게 뭉개져
     * 품질이 낮으므로, 표지는 색상 추출에만 쓰고 책등 이미지는 색상 기반으로 새로 그린다.
     */
    private String buildSvg(Book book, ImageColorExtractor.ExtractedColors colors) {
        return SpineSvgBuilder.buildFromColors(
                book.getTitle(), book.getAuthor(), book.getPublisher(),
                colors.primaryColor(), colors.accentColor());
    }
}
