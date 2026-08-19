package com.booktalk.global.security;

import io.jsonwebtoken.Claims;
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
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

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

    public String createAccessToken(Long userId) {
        return createToken(userId, TOKEN_TYPE_ACCESS, accessTokenExpireMs);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, TOKEN_TYPE_REFRESH, refreshTokenExpireMs);
    }

    /** API 요청 인증 필터에서 사용. Access 토큰이 아니면 예외. */
    public Long parseUserIdFromAccessToken(String token) {
        return parseUserId(token, TOKEN_TYPE_ACCESS);
    }

    /** 토큰 재발급(/auth/refresh)에서 사용. Refresh 토큰이 아니면 예외. */
    public Long parseUserIdFromRefreshToken(String token) {
        return parseUserId(token, TOKEN_TYPE_REFRESH);
    }

    private String createToken(Long userId, String tokenType, long expireMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    private Long parseUserId(String token, String expectedType) {
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

            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("유효하지 않거나 만료된 토큰입니다.");
        }
    }
}
