package com.booktalk.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 소셜 제공자에 따라 이메일 제공 동의가 없을 수 있어 nullable로 둔다 (예: 카카오 비즈 미인증 앱)
	private String email;

	@Column(nullable = false)
	private String nickname;

	private String profileImageUrl;

	@Column(nullable = false)
	private String oauthProvider; // KAKAO, NAVER, GOOGLE, FACEBOOK

	// 각 소셜 제공자가 발급하는 사용자 고유 ID. (oauthProvider, providerId) 조합이 실제 유니크 키.
	@Column(nullable = false)
	private String providerId;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Builder
	public User(String email, String nickname, String profileImageUrl, String oauthProvider, String providerId) {
		this.email = email;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.oauthProvider = oauthProvider;
		this.providerId = providerId;
		this.createdAt = LocalDateTime.now();
	}

	/** 로그인할 때마다 소셜 제공자 쪽 최신 프로필로 갱신한다. */
	public void updateProfile(String nickname, String profileImageUrl) {
		if (nickname != null && !nickname.isBlank()) {
			this.nickname = nickname;
		}
		if (profileImageUrl != null && !profileImageUrl.isBlank()) {
			this.profileImageUrl = profileImageUrl;
		}
	}
}
