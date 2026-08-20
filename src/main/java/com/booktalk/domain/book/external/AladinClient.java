package com.booktalk.domain.book.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * 알라딘 상품검색 API(ItemSearch.aspx) 연동.
 * TTBKey가 없으면 조용히 빈 목록을 반환해서, 로컬 DB 검색만으로도 앱이 정상 동작하게 한다.
 * 알라딘 쪽 장애/응답 이상도 검색 자체를 실패시키지 않고 빈 목록으로 흡수한다(로컬 검색 결과는 보여줘야 하므로).
 *
 * 현재 BookService는 승인 대기가 필요 없는 KakaoBookClient를 사용 중이라 이 클래스는 호출되지 않는다.
 * TTBKey 승인을 받으면 BookService에서 이 클라이언트로 다시 바꾸거나 카카오와 병행할 수 있다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AladinClient {

    private static final String SEARCH_URI = "http://www.aladin.co.kr/ttb/api/ItemSearch.aspx";

    private final AladinProperties properties;
    private final RestClient restClient = RestClient.create();

    public List<ExternalBookInfo> search(String query, int maxResults) {
        if (properties.ttbKey() == null || properties.ttbKey().isBlank()) {
            return List.of();
        }

        String uri = UriComponentsBuilder.fromHttpUrl(SEARCH_URI)
                .queryParam("ttbkey", properties.ttbKey())
                .queryParam("Query", query)
                .queryParam("QueryType", "Keyword")
                .queryParam("MaxResults", maxResults)
                .queryParam("start", 1)
                .queryParam("SearchTarget", "Book")
                .queryParam("output", "js")
                .queryParam("Version", "20131101")
                .queryParam("Cover", "Big")
                .build()
                .toUriString();

        try {
            AladinSearchResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(AladinSearchResponse.class);

            if (response == null || response.item() == null) {
                return List.of();
            }

            return response.item().stream()
                    .map(this::toBookInfo)
                    .toList();
        } catch (RestClientException | RuntimeException e) {
            log.warn("알라딘 API 검색 실패 (query={}): {}", query, e.getMessage());
            return List.of();
        }
    }

    private ExternalBookInfo toBookInfo(AladinItem item) {
        String isbn = (item.isbn13() != null && !item.isbn13().isBlank()) ? item.isbn13() : item.isbn();
        return new ExternalBookInfo(isbn, item.title(), item.author(), item.publisher(), item.cover());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AladinSearchResponse(List<AladinItem> item) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AladinItem(
            String title,
            String author,
            String publisher,
            String isbn,
            String isbn13,
            String cover
    ) {
    }
}
