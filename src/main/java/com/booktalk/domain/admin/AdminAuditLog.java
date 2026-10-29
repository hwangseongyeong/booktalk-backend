package com.booktalk.domain.admin;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 어드민 민감 작업 감사 로그. 삭제·정지·권한 변경 등을 누가·무엇을·어떤 대상에·언제 했는지 남긴다.
 */
@Entity
@Table(name = "admin_audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 작업한 운영자(User.id). */
    @Column(nullable = false)
    private Long adminUserId;

    /** 작업 종류. 예: DELETE_BOOK_NOTE, SUSPEND_USER, GRANT_ADMIN. */
    @Column(nullable = false, length = 50)
    private String action;

    /** 대상 종류. 예: BOOK_NOTE, MEETING_MESSAGE, USER. */
    @Column(nullable = false, length = 50)
    private String targetType;

    /** 대상 식별자(문자열). nullable. */
    @Column(length = 100)
    private String targetId;

    /** 부가 설명(요약). nullable. */
    @Column(length = 500)
    private String detail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public AdminAuditLog(Long adminUserId, String action, String targetType, String targetId, String detail) {
        this.adminUserId = adminUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.detail = detail;
        this.createdAt = LocalDateTime.now();
    }
}
