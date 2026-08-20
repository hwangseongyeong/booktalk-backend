package com.booktalk.domain.book.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 카카오 책 검색 API 인증키(REST API 키).
 * 카카오 로그인에 쓰는 REST API 키와 동일한 값을 사용한다(oauth2.kakao.client-id와 같은 키).
 * 미설정(빈 값) 시에는 카카오 검색을 건너뛰고 로컬 DB 검색만 동작한다.
 */
@ConfigurationProperties(prefix = "kakao")
public record KakaoBookProperties(String restApiKey) {
}
