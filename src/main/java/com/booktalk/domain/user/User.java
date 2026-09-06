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

	// 온보딩에서 고른 프로필 색 (#RRGGBB). 사진 업로드 지원 전까지 아바타 배경으로 쓴다.
	private String profileColor;

	// 로그인 후 온보딩(닉네임/프로필/친구초대)을 마쳤는지. false면 프런트가 온보딩 화면으로 보낸다.
	@Column(nullable = false)
	private boolean onboardingCompleted;

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

	/**
	 * 소셜 제공자 쪽 최신 프로필로 갱신한다.
	 * 온보딩을 마친 사용자는 본인이 정한 닉네임/프로필을 유지해야 하므로 호출 전에 분기한다(AuthService).
	 */
	public void syncOAuthProfile(String nickname, String profileImageUrl) {
		if (nickname != null && !nickname.isBlank()) {
			this.nickname = nickname;
		}
		if (profileImageUrl != null && !profileImageUrl.isBlank()) {
			this.profileImageUrl = profileImageUrl;
		}
	}

	/** 온보딩/프로필 수정 화면에서 사용자가 직접 바꾸는 값. 전달된(널이 아닌) 필드만 반영한다. */
	public void editProfile(String nickname, String profileImageUrl, String profileColor) {
		if (nickname != null && !nickname.isBlank()) {
			this.nickname = nickname.strip();
		}
		if (profileImageUrl != null) {
			this.profileImageUrl = profileImageUrl;
		}
		if (profileColor != null) {
			this.profileColor = profileColor;
		}
	}

	public void completeOnboarding() {
		this.onboardingCompleted = true;
	}
}
