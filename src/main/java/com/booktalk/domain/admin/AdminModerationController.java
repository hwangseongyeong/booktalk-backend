package com.booktalk.domain.admin;

import com.booktalk.domain.admin.dto.AdminBookNoteItem;
import com.booktalk.domain.admin.dto.AdminChatMessageItem;
import com.booktalk.domain.admin.dto.AdminPage;
import com.booktalk.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 어드민 콘텐츠 모더레이션 API. 인가는 SecurityConfig(/api/v1/admin/** → ADMIN)에서 처리.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminModerationController {

    private final AdminModerationService adminModerationService;

    @GetMapping("/book-notes")
    public ApiResponse<AdminPage<AdminBookNoteItem>> bookNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "id"));
        return ApiResponse.success(adminModerationService.listBookNotes(pageable));
    }

    @DeleteMapping("/book-notes/{id}")
    public ApiResponse<Void> deleteBookNote(@PathVariable Long id) {
        adminModerationService.deleteBookNote(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/meetings/{meetingId}/messages")
    public ApiResponse<List<AdminChatMessageItem>> meetingMessages(@PathVariable Long meetingId) {
        return ApiResponse.success(adminModerationService.listMeetingMessages(meetingId));
    }

    @DeleteMapping("/chat-messages/{id}")
    public ApiResponse<Void> deleteChatMessage(@PathVariable Long id) {
        adminModerationService.deleteChatMessage(id);
        return ApiResponse.success(null);
    }
}
