package com.booktalk.domain.admin;

import com.booktalk.global.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 어드민 민감 작업을 감사 로그에 남긴다. 현재 로그인한 운영자(User.id)를 주체로 기록한다.
 */
@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogRepository auditLogRepository;
    private final CurrentUserResolver currentUserResolver;

    @Transactional
    public void record(String action, String targetType, String targetId, String detail) {
        auditLogRepository.save(AdminAuditLog.builder()
                .adminUserId(currentUserResolver.getCurrentUserId())
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detail)
                .build());
    }
}
