package com.booktalk.domain.readingrecord;

import com.booktalk.domain.book.Book;
import com.booktalk.domain.book.BookRepository;
import com.booktalk.domain.readingrecord.dto.ReadingRecordCompleteRequest;
import com.booktalk.domain.readingrecord.dto.ReadingRecordResponse;
import com.booktalk.domain.readingrecord.dto.ReadingRecordStartRequest;
import com.booktalk.domain.user.User;
import com.booktalk.global.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingRecordService {

    private final ReadingRecordRepository readingRecordRepository;
    private final BookRepository bookRepository;
    private final CurrentUserResolver currentUserResolver;

    @Transactional
    public ReadingRecordResponse start(ReadingRecordStartRequest request) {
        User user = currentUserResolver.getCurrentUser();
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다. id=" + request.bookId()));

        ReadingRecord record = ReadingRecord.builder()
                .user(user)
                .book(book)
                .status(ReadingRecord.ReadingStatus.READING)
                .startDate(request.startDate() != null ? request.startDate() : LocalDate.now())
                .build();

        return ReadingRecordResponse.from(readingRecordRepository.save(record));
    }

    @Transactional
    public ReadingRecordResponse complete(Long id, ReadingRecordCompleteRequest request) {
        ReadingRecord record = getOwnedRecord(id);

        record.completeReading(
                request.endDate() != null ? request.endDate() : LocalDate.now(),
                request.rating(),
                request.oneLineNote()
        );

        return ReadingRecordResponse.from(record);
    }

    public List<ReadingRecordResponse> getMyRecords(String status) {
        User user = currentUserResolver.getCurrentUser();

        List<ReadingRecord> records = (status == null || status.isBlank())
                ? readingRecordRepository.findByUserOrderByIdDesc(user)
                : readingRecordRepository.findByUserAndStatusOrderByIdDesc(
                        user, ReadingRecord.ReadingStatus.valueOf(status.toUpperCase()));

        return records.stream().map(ReadingRecordResponse::from).toList();
    }

    private ReadingRecord getOwnedRecord(Long id) {
        User user = currentUserResolver.getCurrentUser();
        ReadingRecord record = readingRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 독서 기록입니다. id=" + id));

        if (!record.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 독서 기록만 수정할 수 있습니다.");
        }

        return record;
    }
}
