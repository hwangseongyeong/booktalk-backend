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

    @GetMapping("/images/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        byte[] bytes = uploadService.read(filename);
        return ResponseEntity.ok()
                .contentType(uploadService.contentTypeOf(filename))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic())
                .body(bytes);
    }
}
