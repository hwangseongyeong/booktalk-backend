package com.booktalk.domain.book;

import com.booktalk.domain.book.dto.BookRegisterRequest;
import com.booktalk.domain.book.dto.BookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    /**
     * 도서 등록. ISBN이 이미 등록되어 있으면 새로 만들지 않고 기존 책을 반환한다(중복 등록 방지).
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

        return BookResponse.from(bookRepository.save(book));
    }

    /**
     * 도서 검색. TODO: 알라딘 API 연동 후 외부 검색 결과와 병합.
     */
    public List<BookResponse> search(String query) {
        List<Book> books = (query == null || query.isBlank())
                ? bookRepository.findAll()
                : bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);

        return books.stream().map(BookResponse::from).toList();
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
