package com.booktalk.domain.upload;

import com.booktalk.domain.upload.dto.UploadResponse;
import com.booktalk.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

/**
 * 이미지 업로드/서빙 API.
 * - POST /api/v1/uploads/images : 멀티파트 이미지 업로드(로그인 필요). 저장 후 접근 URL을 반환.
 * - GET  /api/v1/uploads/images/{filename} : 업로드된 이미지 조회(공개). SecurityConfig에서 permitAll.
 */
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/images")
    public ApiResponse<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = uploadService.store(file);
        return ApiResponse.success(new UploadResponse(url));
    }

    // 키는 날짜 폴더를 포함할 수 있다(예: images 경로 뒤 "20261028/xxxx.jpg").
    // 날짜 폴더가 없던 기존 이미지("xxxx.jpg")도 그대로 받는다. {*key}로 하위 경로 전체를 캡처한다.
    @GetMapping("/images/{*key}")
    public ResponseEntity<byte[]> getImage(@PathVariable String key) {
        String relKey = key.startsWith("/") ? key.substring(1) : key;
        byte[] bytes = uploadService.read(relKey);
        return ResponseEntity.ok()
                .contentType(uploadService.contentTypeOf(relKey))
                // 파일명이 UUID라 한 URL의 내용은 바뀌지 않는다. immutable로 브라우저 재검증까지 생략시켜
                // 클라이언트 캐시를 최대한 활용(→ 재요청/S3 다운로드 최소화).
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(bytes);
    }
}
