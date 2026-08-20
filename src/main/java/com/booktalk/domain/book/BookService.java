package com.booktalk.domain.book;

import com.booktalk.domain.book.dto.BookRegisterRequest;
import com.booktalk.domain.book.dto.BookResponse;
import com.booktalk.domain.book.dto.BookSearchResultResponse;
import com.booktalk.domain.book.external.ExternalBookInfo;
import com.booktalk.domain.book.external.KakaoBookClient;
import com.booktalk.domain.book.spine.SpineAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private static final int EXTERNAL_SEARCH_MAX_RESULTS = 10;
    private static final String EXTERNAL_SOURCE_KAKAO = "KAKAO";

    private final BookRepository bookRepository;
    private final KakaoBookClient kakaoBookClient;
    private final SpineAssetService spineAssetService;

    /**
     * 도서 등록. ISBN이 이미 등록되어 있으면 새로 만들지 않고 기존 책을 반환한다(중복 등록 방지).
     * 카카오 검색 결과를 등록할 때도 이 API를 그대로 사용한다.
     * 등록 시 표지 이미지에서 대표색을 뽑아 책등 SVG를 생성한다(실패해도 등록은 진행됨).
     */
    @Transactional
    public BookResponse register(BookRegisterRequest request) {
        if (request.isbn() != null && !request.isbn().isBlank()) {
            var existing = bookRepository.findByIsbn(request.isbn());
            if (existing.isPresent()) {
                return BookResponse.from(existing.get());
            }
        }

        Book book = Book.builder()
                .isbn(blankToNull(request.isbn()))
                .title(request.title())
                .author(request.author())
                .publisher(request.publisher())
                .coverImageUrl(request.coverImageUrl())
                .pageCount(request.pageCount())
                .build();

        bookRepository.save(book); // ID 채번 (책등 이미지 저장 키로 사용)
        spineAssetService.generateAndAttach(book); // 영속 상태 엔티티 필드만 갱신, 트랜잭션 커밋 시 자동 반영

        return BookResponse.from(book);
    }

    /**
     * 도서 검색. 로컬 DB(이미 등록된 책) 결과를 먼저 보여주고, 검색어가 있으면 카카오 책 검색 결과를 이어붙인다.
     * 카카오 결과 중 이미 로컬에 등록된(ISBN 동일) 책은 중복으로 보여주지 않는다.
     * 카카오 API가 실패해도 로컬 검색 결과는 그대로 반환된다.
     */
    public List<BookSearchResultResponse> search(String query) {
        List<Book> localBooks = (query == null || query.isBlank())
                ? bookRepository.findAll()
                : bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);

        List<BookSearchResultResponse> results = new ArrayList<>(
                localBooks.stream().map(BookSearchResultResponse::fromLocal).toList()
        );

        if (query != null && !query.isBlank()) {
            Set<String> localIsbns = new HashSet<>();
            for (Book book : localBooks) {
                if (book.getIsbn() != null) {
                    localIsbns.add(book.getIsbn());
                }
            }

            List<ExternalBookInfo> kakaoResults = kakaoBookClient.search(query, EXTERNAL_SEARCH_MAX_RESULTS);
            for (ExternalBookInfo info : kakaoResults) {
                if (info.isbn() != null && localIsbns.contains(info.isbn())) {
                    continue;
                }
                results.add(BookSearchResultResponse.fromExternal(EXTERNAL_SOURCE_KAKAO, info));
            }
        }

        return results;
    }

    public BookResponse getById(Long id) {
        return bookRepository.findById(id)
                .map(BookResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다. id=" + id));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
