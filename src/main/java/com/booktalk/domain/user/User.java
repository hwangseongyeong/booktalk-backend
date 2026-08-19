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

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String nickname;

	private String profileImageUrl;

	@Column(nullable = false)
	private String oauthProvider; // KAKAO, GOOGLE 등

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Builder
	public User(String email, String nickname, String profileImageUrl, String oauthProvider) {
		this.email = email;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.oauthProvider = oauthProvider;
		this.createdAt = LocalDateTime.now();
	}
}
