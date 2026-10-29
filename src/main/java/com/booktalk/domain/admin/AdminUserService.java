package com.booktalk.domain.admin;

import com.booktalk.domain.admin.dto.AdminPage;
import com.booktalk.domain.admin.dto.AdminUserDetail;
import com.booktalk.domain.admin.dto.AdminUserListItem;
import com.booktalk.domain.booknote.BookNoteRepository;
import com.booktalk.domain.readingrecord.ReadingRecordRepository;
import com.booktalk.domain.user.User;
import com.booktalk.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 어드민 회원 관리. 목록·검색·상세 조회와 정지/해제·권한 부여/회수(감사 기록 포함).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final ReadingRecordRepository readingRecordRepository;
    private final BookNoteRepository bookNoteRepository;
    private final AdminAuditService adminAuditService;

    public AdminPage<AdminUserListItem> list(String query, Pageable pageable) {
        Page<User> page = (query == null || query.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByNicknameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        query.strip(), query.strip(), pageable);
        return AdminPage.of(page.map(AdminUserListItem::from));
    }

    public AdminUserDetail detail(Long id) {
        User user = getUser(id);
        return AdminUserDetail.of(
                user,
                readingRecordRepository.countByUser(user),
                bookNoteRepository.countByUser(user));
    }

    @Transactional
    public void suspend(Long id) {
        User user = getUser(id);
        user.suspend();
        adminAuditService.record("SUSPEND_USER", "USER", String.valueOf(id), user.getNickname());
    }

    @Transactional
    public void unsuspend(Long id) {
        User user = getUser(id);
        user.unsuspend();
        adminAuditService.record("UNSUSPEND_USER", "USER", String.valueOf(id), user.getNickname());
    }

    @Transactional
    public void grantAdmin(Long id) {
        User user = getUser(id);
        user.grantAdmin();
        adminAuditService.record("GRANT_ADMIN", "USER", String.valueOf(id), user.getNickname());
    }

    @Transactional
    public void revokeAdmin(Long id) {
        User user = getUser(id);
        user.revokeAdmin();
        adminAuditService.record("REVOKE_ADMIN", "USER", String.valueOf(id), user.getNickname());
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
