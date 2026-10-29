package com.booktalk.domain.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByOauthProviderAndProviderId(String oauthProvider, String providerId);

	// ---------- 어드민 지표/목록 ----------
	long countByCreatedAtAfter(LocalDateTime time);

	Page<User> findByNicknameContainingIgnoreCaseOrEmailContainingIgnoreCase(
			String nickname, String email, Pageable pageable);
}
