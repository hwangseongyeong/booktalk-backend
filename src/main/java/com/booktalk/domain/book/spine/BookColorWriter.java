package com.booktalk.domain.book.spine;

import com.booktalk.domain.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 책등 색상 저장만 담당하는 짧은 트랜잭션 경계.
 * SpineAssetService의 비동기 색상 추출이 느린 외부 호출을 트랜잭션 밖에서 끝낸 뒤,
 * DB 쓰기만 이 빈을 통해 짧게 수행한다(외부 호출 동안 커넥션 점유 방지).
 * 별도 빈으로 둔 이유: 같은 클래스 내부 호출은 @Transactional 프록시가 적용되지 않기 때문.
 */
@Component
@RequiredArgsConstructor
public class BookColorWriter {

    private final BookRepository bookRepository;

    @Transactional
    public void save(Long bookId, String primaryColor, String accentColor) {
        bookRepository.findById(bookId).ifPresent(book -> {
            // 그 사이 다른 경로로 색이 채워졌으면 덮어쓰지 않는다(책당 1회).
            if (book.getPrimaryColor() == null || book.getAccentColor() == null) {
                book.updateSpineAssets(null, primaryColor, accentColor);
            }
        });
    }
}
