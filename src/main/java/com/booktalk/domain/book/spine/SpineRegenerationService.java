package com.booktalk.domain.book.spine;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.book.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 기존에 저장된 모든 책의 책등을 현재 로직으로 다시 생성한다(강제 재생성).
 * 책등 디자인이 바뀌었을 때 기존 책들을 일괄로 새 스타일로 갱신하는 유지보수용.
 *
 * generateAndAttach는 실패 허용 정책이라(표지 실패 시 색상 폴백, 업로드 실패 시 URL null)
 * 개별 책 처리는 예외를 거의 던지지 않지만, 방어적으로 감싸서 한 권이 실패해도 나머지는 진행한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpineRegenerationService {

    private final BookRepository bookRepository;
    private final SpineAssetService spineAssetService;

    @Transactional
    public RegenResult regenerateAll() {
        List<Book> books = bookRepository.findAll();
        int regenerated = 0;
        int failed = 0;
        List<String> sampleUrls = new ArrayList<>();

        for (Book book : books) {
            try {
                spineAssetService.generateAndAttach(book);
                regenerated++;
                if (sampleUrls.size() < 3 && book.getSpineImageUrl() != null) {
                    sampleUrls.add(book.getSpineImageUrl());
                }
            } catch (Exception e) {
                failed++;
                log.warn("책등 재생성 실패 (bookId={}): {}", book.getId(), e.getMessage());
            }
        }

        log.info("책등 일괄 재생성 완료: total={}, regenerated={}, failed={}", books.size(), regenerated, failed);
        return new RegenResult(books.size(), regenerated, failed, sampleUrls);
    }

    public record RegenResult(int total, int regenerated, int failed, List<String> sampleUrls) {
    }
}
