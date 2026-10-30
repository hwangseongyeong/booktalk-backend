package com.booktalk.domain.upload.dto;

/**
 * 일반 파일 업로드 응답.
 * - url: 접근/다운로드 경로(상대경로 또는 CDN 절대 URL). 프런트는 resolveAssetUrl로 절대화.
 * - name: 원본 파일명(다운로드 시 표시/저장 파일명).
 * - size: 바이트 크기.
 * - contentType: 업로드된 MIME 타입(참고용).
 */
public record UploadFileResponse(String url, String name, long size, String contentType) {
}
