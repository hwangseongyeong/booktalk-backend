package com.booktalk.domain.readingrecord;

import com.booktalk.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReadingRecordRepository extends JpaRepository<ReadingRecord, Long> {

    List<ReadingRecord> findByUserOrderByIdDesc(User user);

    List<ReadingRecord> findByUserAndStatusOrderByIdDesc(User user, ReadingRecord.ReadingStatus status);

    // 월별 서재 집계용: 특정 사용자가 특정 기간 안에 완독한 기록
    List<ReadingRecord> findByUserAndStatusAndEndDateBetweenOrderByEndDateAsc(
            User user, ReadingRecord.ReadingStatus status, LocalDate from, LocalDate to);
}
