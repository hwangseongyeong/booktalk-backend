package com.booktalk.domain.book.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * 카카오 책 검색 API(dapi.kakao.com/v3/search/book) 연동.
 * REST API 키가 없으면 조용히 빈 목록을 반환해서, 로컬 DB 검색만으로도 앱이 정상 동작하게 한다.
 * 카카오 쪽 장애/응답 이상도 검색 자체를 실패시키지 않고 빈 목록으로 흡수한다(로컬 검색 결과는 보여줘야 하므로).
 *
 * 검색어가 ISBN 형태(숫자 10/13자리)면 target=isbn으로, 그 외에는 통합 검색(query)으로 조회한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoBookClient {

    private static final String SEARCH_URI = "https://dapi.kakao.com/v3/search/book";

    private final KakaoBookProperties properties;
    private final RestClient restClient = RestClient.create();

    public List<KakaoBookInfo> search(String query, int maxResults) {
        if (properties.restApiKey() == null || properties.restApiKey().isBlank()) {
            return List.of();
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SEARCH_URI)
                .queryParam("query", query)
                .queryParam("size", maxResults);

        if (isIsbn(query)) {
            builder.queryParam("target", "isbn");
        }

        String uri = builder.build(false).toUriString();

        try {
            KakaoBookSearchResponse response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.restApiKey())
                    .retrieve()
                    .body(KakaoBookSearchResponse.class);

            if (response == null || response.documents() == null) {
                return List.of();
            }

            return response.documents().stream()
                    .map(this::toBookInfo)
                    .toList();
        } catch (RuntimeException e) {
            log.warn("카카오 책 검색 실패 (query={}): {}", query, e.getMessage());
            return List.of();
        }
    }

    private boolean isIsbn(String query) {
        String digits = query.replaceAll("[\\s-]", "");
        return digits.matches("\\d{10}|\\d{13}");
    }

    private KakaoBookInfo toBookInfo(KakaoDocument doc) {
        return new KakaoBookInfo(
                pickIsbn(doc.isbn()),
                doc.title(),
                joinAuthors(doc.authors()),
                blankToNull(doc.publisher()),
                blankToNull(doc.thumbnail())
        );
    }

    /**
     * 카카오 isbn 필드는 "ISBN10 ISBN13" 형태로 두 값이 공백으로 붙어올 수 있다.
     * 13자리(978/979)를 우선 사용하고, 없으면 첫 번째 값을 쓴다.
     */
    private String pickIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return null;
        }
        String[] tokens = isbn.trim().split("\\s+");
        for (String token : tokens) {
            if (token.length() == 13) {
                return token;
            }
        }
        return tokens[0];
    }

    private String joinAuthors(List<String> authors) {
        if (authors == null || authors.isEmpty()) {
            return null;
        }
        return String.join(", ", authors);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoBookSearchResponse(List<KakaoDocument> documents) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoDocument(
            String title,
            List<String> authors,
            String publisher,
            String isbn,
            String thumbnail
    ) {
    }
}
