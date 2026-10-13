package com.booktalk.domain.book;

import com.booktalk.domain.book.dto.BookRegisterRequest;
import com.booktalk.domain.book.dto.BookResponse;
import com.booktalk.domain.book.dto.BookSearchResultResponse;
import com.booktalk.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ApiResponse<BookResponse> register(@Valid @RequestBody BookRegisterRequest request) {
        return ApiResponse.success(bookService.register(request));
    }

    /** 도서 검색. 로컬 DB 등록 여부(id 유무)와 카카오 책 검색 결과를 함께 내려준다. */
    @GetMapping
    public ApiResponse<List<BookSearchResultResponse>> search(@RequestParam(required = false) String query) {
        return ApiResponse.success(bookService.search(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(bookService.getById(id));
    }

    /**
     * 책등 이미지를 실시간 렌더해 SVG로 내려준다(저장하지 않음, 색은 등록/독서 기록 시 캐싱된 값 사용).
     * 같은 색이면 결과가 불변이므로 ETag + 장기 캐시로 브라우저/CDN에 태운다.
     * 프런트는 {@code <img src="/api/v1/books/{id}/spine.svg">} 로 바로 사용한다.
     */
    @GetMapping(value = "/{id}/spine.svg", produces = "image/svg+xml")
    public ResponseEntity<String> spineSvg(@PathVariable Long id) {
        String svg = bookService.renderSpineSvg(id);
        String etag = "\"" + Integer.toHexString(svg.hashCode()) + "\"";
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .contentType(MediaType.valueOf("image/svg+xml;charset=UTF-8"))
                .body(svg);
    }
}
