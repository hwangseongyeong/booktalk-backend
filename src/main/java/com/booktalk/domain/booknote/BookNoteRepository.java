package com.booktalk.domain.booknote;

import com.booktalk.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookNoteRepository extends JpaRepository<BookNote, Long> {

    // 사용자 피드: soft-delete된 기록은 숨긴다.
    List<BookNote> findByUserAndDeletedFalseOrderByIdDesc(User user);

    long countByUser(User user);

    // 어드민 모더레이션: 삭제되지 않은 전체 기록(최신순).
    Page<BookNote> findByDeletedFalseOrderByIdDesc(Pageable pageable);
}
