package com.booktalk.domain.book;

import com.booktalk.domain.book.dto.BookRegisterRequest;
import com.booktalk.domain.book.dto.BookResponse;
import com.booktalk.domain.book.dto.BookSearchResultResponse;
import com.booktalk.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
