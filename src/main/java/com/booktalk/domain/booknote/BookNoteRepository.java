package com.booktalk.domain.booknote;

import com.booktalk.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookNoteRepository extends JpaRepository<BookNote, Long> {

    List<BookNote> findByUserOrderByIdDesc(User user);
}
