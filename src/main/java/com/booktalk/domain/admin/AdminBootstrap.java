package com.booktalk.domain.admin;

import com.booktalk.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 초기 관리자 승격. {@code admin.bootstrap-emails}(쉼표 구분)에 적힌 이메일의 사용자를
 * 앱 시작 시 ADMIN으로 올린다. 이미 ADMIN이면 건너뛴다(idempotent).
 *
 * 운영 흐름: 관리자가 먼저 앱에 로그인해 User가 생성된 뒤, 그 이메일을 env에 넣고 재기동하면 승격된다.
 * 카카오 이메일 동의가 없어 이메일이 비는 경우엔 DB에서 직접 role=ADMIN으로 UPDATE 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;

    @Value("${admin.bootstrap-emails:}")
    private String bootstrapEmails;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (bootstrapEmails == null || bootstrapEmails.isBlank()) {
            return;
        }
        for (String raw : bootstrapEmails.split(",")) {
            String email = raw.strip();
            if (email.isEmpty()) {
                continue;
            }
            userRepository.findByEmail(email).ifPresentOrElse(user -> {
                if (!user.isAdmin()) {
                    user.grantAdmin();
                    log.info("[AdminBootstrap] {} → ADMIN 승격", email);
                }
            }, () -> log.warn("[AdminBootstrap] {} 사용자를 찾지 못해 승격 생략(먼저 로그인 필요)", email));
        }
    }
}
