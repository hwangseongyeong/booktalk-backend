package com.booktalk.domain.admin;

import com.booktalk.domain.admin.dto.AdminBookNoteItem;
import com.booktalk.domain.admin.dto.AdminChatMessageItem;
import com.booktalk.domain.admin.dto.AdminPage;
import com.booktalk.domain.booknote.BookNote;
import com.booktalk.domain.booknote.BookNoteRepository;
import com.booktalk.domain.meeting.Meeting;
import com.booktalk.domain.meeting.MeetingRepository;
import com.booktalk.domain.meeting.chat.MeetingChatMessage;
import com.booktalk.domain.meeting.chat.MeetingChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 어드민 콘텐츠 모더레이션. 혼자 북톡(기록)·모임 채팅 메시지 조회 + soft-delete(감사 기록 포함).
 */
@Service
@RequiredArgsConstructor
public class AdminModerationService {

    private final BookNoteRepository bookNoteRepository;
    private final MeetingChatMessageRepository chatMessageRepository;
    private final MeetingRepository meetingRepository;
    private final AdminAuditService adminAuditService;

    @Transactional(readOnly = true)
    public AdminPage<AdminBookNoteItem> listBookNotes(Pageable pageable) {
        return AdminPage.of(bookNoteRepository.findByDeletedFalseOrderByIdDesc(pageable)
                .map(AdminBookNoteItem::from));
    }

    @Transactional
    public void deleteBookNote(Long id) {
        BookNote note = bookNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("기록을 찾을 수 없습니다."));
        note.softDelete();
        adminAuditService.record("DELETE_BOOK_NOTE", "BOOK_NOTE", String.valueOf(id), preview(note.getContent()));
    }

    @Transactional(readOnly = true)
    public List<AdminChatMessageItem> listMeetingMessages(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));
        return chatMessageRepository.findByMeetingOrderByIdDesc(meeting).stream()
                .map(AdminChatMessageItem::from)
                .toList();
    }

    @Transactional
    public void deleteChatMessage(Long id) {
        MeetingChatMessage message = chatMessageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));
        message.softDelete();
        adminAuditService.record("DELETE_CHAT_MESSAGE", "MEETING_MESSAGE", String.valueOf(id), preview(message.getContent()));
    }

    private static String preview(String s) {
        if (s == null) return null;
        return s.length() > 100 ? s.substring(0, 100) : s;
    }
}
