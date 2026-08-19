package com.booktalk.domain.auth.client;

import com.booktalk.domain.auth.OAuthProvider;
import com.booktalk.domain.auth.OAuthUserInfo;

/**
 * 인가 코드(code)를 받아 제공자의 토큰 엔드포인트/사용자 정보 엔드포인트를 직접 호출한다.
 * redirectUri: 카카오/구글/페이스북에서 사용 (프런트가 실제로 리다이렉트에 사용한 값과 일치해야 함)
 * state: 네이버에서 사용 (CSRF 방지용, 프런트가 로그인 시작 시 생성해서 콜백까지 들고 옴)
 */
public interface OAuthClient {

    OAuthProvider provider();

    OAuthUserInfo fetchUserInfo(String code, String redirectUri, String state);
}
