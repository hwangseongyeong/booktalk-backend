package com.booktalk.domain.booknote;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.book.BookRepository;
import com.booktalk.domain.booknote.dto.BookNoteCreateRequest;
import com.booktalk.domain.booknote.dto.BookNoteResponse;
import com.booktalk.domain.user.User;
import com.booktalk.global.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookNoteService {

    private final BookNoteRepository bookNoteRepository;
    private final BookRepository bookRepository;
    private final CurrentUserResolver currentUserResolver;

    @Transactional
    public BookNoteResponse create(BookNoteCreateRequest request) {
        User user = currentUserResolver.getCurrentUser();

        String content = request.content() != null ? request.content().trim() : null;
        String imageUrl = request.imageUrl() != null ? request.imageUrl().trim() : null;
        boolean hasContent = content != null && !content.isBlank();
        boolean hasImage = imageUrl != null && !imageUrl.isBlank();
        if (!hasContent && !hasImage) {
            throw new IllegalArgumentException("사진 또는 글 중 하나는 있어야 합니다.");
        }

        Book book = null;
        if (request.bookId() != null) {
            book = bookRepository.findById(request.bookId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다. id=" + request.bookId()));
        }

        BookNote note = BookNote.builder()
                .user(user)
                .book(book)
                .imageUrl(hasImage ? imageUrl : null)
                .content(hasContent ? content : null)
                .build();

        return BookNoteResponse.from(bookNoteRepository.save(note));
    }

    public List<BookNoteResponse> getMyNotes() {
        User user = currentUserResolver.getCurrentUser();
        return bookNoteRepository.findByUserAndDeletedFalseOrderByIdDesc(user).stream()
                .map(BookNoteResponse::from)
                .toList();
    }

    /** 혼자 기록 단건 조회. 본인 소유 + 삭제되지 않은 기록만. */
    public BookNoteResponse getMyNote(Long id) {
        User user = currentUserResolver.getCurrentUser();
        BookNote note = bookNoteRepository.findByIdAndUserAndDeletedFalse(id, user)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다. id=" + id));
        return BookNoteResponse.from(note);
    }

    @Transactional
    public void delete(Long id) {
        User user = currentUserResolver.getCurrentUser();
        BookNote note = bookNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다. id=" + id));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 기록만 삭제할 수 있습니다.");
        }

        bookNoteRepository.delete(note);
    }
}
