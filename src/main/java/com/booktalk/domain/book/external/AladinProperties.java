package com.booktalk.domain.book.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 알라딘 오픈API 인증키.
 * https://blog.aladin.co.kr/openapi/ 에서 TTBKey 발급.
 * 미설정(빈 값) 시에는 알라딘 검색을 건너뛰고 로컬 DB 검색만 동작한다.
 */
@ConfigurationProperties(prefix = "aladin")
public record AladinProperties(String ttbKey) {
}
