package com.booktalk.domain.user;

/**
 * 사용자 역할. 어드민 웹/어드민 API 접근 권한의 기준이다.
 * - USER: 일반 사용자(기본값)
 * - ADMIN: 운영자. /api/v1/admin/** 접근 가능.
 */
public enum UserRole {
    USER,
    ADMIN
}
