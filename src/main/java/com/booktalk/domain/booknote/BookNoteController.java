package com.booktalk.domain.booknote;

import com.booktalk.domain.booknote.dto.BookNoteCreateRequest;
import com.booktalk.domain.booknote.dto.BookNoteResponse;
import com.booktalk.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 혼자 기록 API. 로그인한 사용자의 개인 기록(사진/글)을 남기고 조회/삭제한다.
 */
@RestController
@RequestMapping("/api/v1/book-notes")
@RequiredArgsConstructor
public class BookNoteController {

    private final BookNoteService bookNoteService;

    /** 혼자 기록 작성 */
    @PostMapping
    public ApiResponse<BookNoteResponse> create(@RequestBody BookNoteCreateRequest request) {
        return ApiResponse.success(bookNoteService.create(request));
    }

    /** 내 혼자 기록 목록(최신순) */
    @GetMapping
    public ApiResponse<List<BookNoteResponse>> getMyNotes() {
        return ApiResponse.success(bookNoteService.getMyNotes());
    }

    /** 혼자 기록 단건 조회(본인 것만) */
    @GetMapping("/{id}")
    public ApiResponse<BookNoteResponse> getNote(@PathVariable Long id) {
        return ApiResponse.success(bookNoteService.getMyNote(id));
    }

    /** 혼자 기록 삭제 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookNoteService.delete(id);
        return ApiResponse.success();
    }
}
