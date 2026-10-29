package com.booktalk.domain.admin;

import com.booktalk.domain.admin.dto.AdminMetricsResponse;
import com.booktalk.domain.book.BookRepository;
import com.booktalk.domain.booknote.BookNoteRepository;
import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.meeting.MeetingRepository;
import com.booktalk.domain.meeting.chat.MeetingChatMessageRepository;
import com.booktalk.domain.readingrecord.ReadingRecord;
import com.booktalk.domain.readingrecord.ReadingRecordRepository;
import com.booktalk.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 어드민 대시보드 지표 집계. 전부 count 쿼리라 읽기 전용.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMetricsService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReadingRecordRepository readingRecordRepository;
    private final MeetingRepository meetingRepository;
    private final BookNoteRepository bookNoteRepository;
    private final MeetingChatMessageRepository meetingChatMessageRepository;

    public AdminMetricsResponse getMetrics() {
        LocalDate today = LocalDate.now(ZONE);
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime start7d = today.minusDays(6).atStartOfDay();

        return new AdminMetricsResponse(
                userRepository.count(),
                userRepository.countByCreatedAtAfter(startOfToday),
                userRepository.countByCreatedAtAfter(start7d),
                bookRepository.count(),
                readingRecordRepository.countByStatus(ReadingRecord.ReadingStatus.READING),
                readingRecordRepository.countByStatus(ReadingRecord.ReadingStatus.COMPLETED),
                readingRecordRepository.countByStatus(ReadingRecord.ReadingStatus.WISHLIST),
                meetingRepository.count(),
                meetingRepository.countByStatus(Meeting.MeetingStatus.RECRUITING),
                meetingRepository.countByStatus(Meeting.MeetingStatus.ONGOING),
                meetingRepository.countByStatus(Meeting.MeetingStatus.CLOSED),
                bookNoteRepository.count(),
                meetingChatMessageRepository.count()
        );
    }
}
