package com.booktalk.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * BookTalk 자체 JWT 발급/검증.
 * 소셜 로그인(카카오/네이버/구글/페이스북)은 신원 확인에만 사용하고,
 * 이후 API 인증은 이 클래스가 발급한 access/refresh 토큰으로 처리한다.
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String CLAIM_ROLE = "role";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";
    private static final String DEFAULT_ROLE = "USER";

    /** Access 토큰에서 꺼낸 인증 주체(userId)와 역할(role). */
    public record AccessTokenPayload(Long userId, String role) {}

    private final SecretKey key;
    private final long accessTokenExpireMs;
    private final long refreshTokenExpireMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expire-ms}") long accessTokenExpireMs,
            @Value("${jwt.refresh-token-expire-ms}") long refreshTokenExpireMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpireMs = accessTokenExpireMs;
        this.refreshTokenExpireMs = refreshTokenExpireMs;
    }

    /** 역할 미지정(일반 사용자) Access 토큰. */
    public String createAccessToken(Long userId) {
        return createAccessToken(userId, DEFAULT_ROLE);
    }

    /** 역할 클레임(role)을 포함한 Access 토큰. 어드민 권한 가드의 근거가 된다. */
    public String createAccessToken(Long userId, String role) {
        return createToken(userId, TOKEN_TYPE_ACCESS, accessTokenExpireMs, role);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, TOKEN_TYPE_REFRESH, refreshTokenExpireMs, null);
    }

    /** 인증 필터에서 사용. Access 토큰이 아니면 예외. userId와 role을 함께 돌려준다. */
    public AccessTokenPayload parseAccessToken(String token) {
        Claims claims = parseClaims(token, TOKEN_TYPE_ACCESS);
        String role = claims.get(CLAIM_ROLE, String.class);
        return new AccessTokenPayload(Long.valueOf(claims.getSubject()), role != null ? role : DEFAULT_ROLE);
    }

    /** API 요청 인증 필터 호환용(role 불필요 시). Access 토큰이 아니면 예외. */
    public Long parseUserIdFromAccessToken(String token) {
        return Long.valueOf(parseClaims(token, TOKEN_TYPE_ACCESS).getSubject());
    }

    /** 토큰 재발급(/auth/refresh)에서 사용. Refresh 토큰이 아니면 예외. */
    public Long parseUserIdFromRefreshToken(String token) {
        return Long.valueOf(parseClaims(token, TOKEN_TYPE_REFRESH).getSubject());
    }

    private String createToken(Long userId, String tokenType, long expireMs, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMs);

        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key);
        if (role != null) {
            builder.claim(CLAIM_ROLE, role);
        }
        return builder.compact();
    }

    private Claims parseClaims(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            if (!expectedType.equals(tokenType)) {
                throw new InvalidTokenException("잘못된 토큰 타입입니다.");
            }
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("유효하지 않거나 만료된 토큰입니다.");
        }
    }
}
