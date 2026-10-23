package com.booktalk.domain.upload.dto;

/**
 * 이미지 업로드 응답. url은 프론트에서 그대로 저장/표시할 수 있는 경로다.
 * 상대 경로(/api/v1/uploads/images/{파일명})로 내려주고, 프론트의 resolveAssetUrl이
 * API_ORIGIN을 붙여 절대 URL로 만든다.
 */
public record UploadResponse(String url) {
}
