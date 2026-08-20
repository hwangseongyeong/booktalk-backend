package com.booktalk.domain.book.external;

import com.booktalk.domain.auth.client.property.KakaoProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * 카카오 책 검색 API(다음 책 검색) 연동.
 * 카카오 로그인에 쓰는 REST API 키(oauth2.kakao.client-id)를 그대로 재사용한다 —
 * 카카오 개발자 콘솔에서 발급하는 REST API 키는 로그인(client_id)과
 * 각종 카카오 API 호출(Authorization: KakaoAK) 양쪽에 공통으로 쓰인다.
 * 별도 승인 절차가 없어 알라딘과 달리 키만 있으면 바로 동작한다.
 *
 * 키가 없거나 API 호출이 실패해도 예외를 던지지 않고 빈 목록을 반환해서 로컬 검색은 항상 살아있게 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoBookClient {

    private static final String SEARCH_URI = "https://dapi.kakao.com/v3/search/book";

    private final KakaoProperties kakaoProperties;
    private final RestClient restClient = RestClient.create();

    public List<ExternalBookInfo> search(String query, int size) {
        String apiKey = kakaoProperties.clientId();
        if (apiKey == null || apiKey.isBlank()) {
            return List.of();
        }

        String uri = UriComponentsBuilder.fromHttpUrl(SEARCH_URI)
                .queryParam("query", query)
                .queryParam("size", size)
                .build()
                .toUriString();

        try {
            KakaoBookSearchResponse response = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + apiKey)
                    .retrieve()
                    .body(KakaoBookSearchResponse.class);

            if (response == null || response.documents() == null) {
                return List.of();
            }

            return response.documents().stream()
                    .map(this::toBookInfo)
                    .toList();
        } catch (RestClientException | RuntimeException e) {
            log.warn("카카오 책 검색 실패 (query={}): {}", query, e.getMessage());
            return List.of();
        }
    }

    private ExternalBookInfo toBookInfo(KakaoBookDocument doc) {
        String isbn = extractIsbn13(doc.isbn());
        String author = (doc.authors() != null && !doc.authors().isEmpty())
                ? String.join(", ", doc.authors())
                : null;
        return new ExternalBookInfo(isbn, stripHtmlTags(doc.title()), author, doc.publisher(), doc.thumbnail());
    }

    // 카카오는 isbn 필드에 "isbn10 isbn13"이 공백으로 같이 내려온다. 13자리를 우선 사용.
    private String extractIsbn13(String isbnField) {
        if (isbnField == null || isbnField.isBlank()) {
            return null;
        }
        String[] parts = isbnField.trim().split("\\s+");
        for (String part : parts) {
            if (part.length() == 13) {
                return part;
            }
        }
        return parts[0];
    }

    // 카카오는 검색어가 title에 포함되면 <b>...</b> 로 하이라이트해서 내려주므로 태그를 제거한다.
    private String stripHtmlTags(String text) {
        return text == null ? null : text.replaceAll("<[^>]*>", "");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoBookSearchResponse(List<KakaoBookDocument> documents) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record KakaoBookDocument(
            String title,
            List<String> authors,
            String publisher,
            String isbn,
            String thumbnail
    ) {
    }
}
