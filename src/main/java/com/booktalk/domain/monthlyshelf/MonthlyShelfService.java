package com.booktalk.domain.monthlyshelf;

import com.booktalk.domain.monthlyshelf.dto.MonthlyShelfResponse;
import com.booktalk.domain.monthlyshelf.dto.ShelfBookItem;
import com.booktalk.domain.readingrecord.ReadingRecord;
import com.booktalk.domain.readingrecord.ReadingRecordRepository;
import com.booktalk.domain.user.User;
import com.booktalk.global.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 월별 서재(책꽂이 시각화 모드1) 데이터를 만든다.
 * 별도 캐시 테이블(monthly_shelf) 없이 reading_records를 그때그때 집계한다.
 * 데이터가 커져서 성능이 문제되면 그때 캐시 테이블 도입을 검토한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyShelfService {

    private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ReadingRecordRepository readingRecordRepository;
    private final CurrentUserResolver currentUserResolver;

    public MonthlyShelfResponse getMonthlyShelf(String yearMonth) {
        User user = currentUserResolver.getCurrentUser();
        YearMonth ym = parseYearMonth(yearMonth);

        List<ReadingRecord> records = readingRecordRepository
                .findByUserAndStatusAndEndDateBetweenOrderByEndDateAsc(
                        user, ReadingRecord.ReadingStatus.COMPLETED, ym.atDay(1), ym.atEndOfMonth());

        List<ShelfBookItem> items = records.stream().map(ShelfBookItem::from).toList();

        return new MonthlyShelfResponse(ym.format(YEAR_MONTH_FORMAT), items.size(), items);
    }

    private YearMonth parseYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(yearMonth, YEAR_MONTH_FORMAT);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("yearMonth 형식이 올바르지 않습니다. 예: 2026-07");
        }
    }
}
