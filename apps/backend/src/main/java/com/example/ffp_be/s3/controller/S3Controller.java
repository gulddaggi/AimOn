package com.example.ffp_be.s3.controller;

import com.example.ffp_be.s3.dto.UploadUrlResponseDto;
import com.example.ffp_be.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/profile/upload-url")
    @Operation(summary = "프로필 이미지 업로드 url 발급")
    public UploadUrlResponseDto getProfileUploadUrl(
        @RequestParam Long userId,
        @RequestParam String contentType
    ) {
        String key = String.format("prod/users/%d/profile.jpg", userId);
        String uploadUrl = s3Service.generateUploadUrl(key, contentType);
        return new UploadUrlResponseDto(uploadUrl, key);
    }

    // Download Presigned URL
    @GetMapping("/profile/download-url")
    @Operation(summary = "프로필 이미지 다운로드 url 발급")
    public String getProfileDownloadUrl(@RequestParam String key) {
        return s3Service.generateDownloadUrl(key);
    }

    @GetMapping("/posts/upload-url")
    @Operation(summary = "게시글 이미지 업로드 url 발급")
    public UploadUrlResponseDto getPostImageUploadUrl(
        @RequestParam Long userId,
        @RequestParam String contentType
    ) {
        // 허용된 이미지 타입만 처리
        Map<String, String> extByType = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
        );
        if (!extByType.containsKey(contentType)) {
            throw new IllegalArgumentException("Unsupported content type");
        }
        String extension = extByType.get(contentType);
        String key = String.format("prod/posts/%d/%s%s", userId, UUID.randomUUID(), extension);
        String uploadUrl = s3Service.generateUploadUrl(key, contentType);
        return new UploadUrlResponseDto(uploadUrl, key);
    }

    @GetMapping("/posts/download-url")
    @Operation(summary = "게시글 이미지 다운로드 url 발급")
    public String getPostImageDownloadUrl(@RequestParam String key) {
        return s3Service.generateDownloadUrl(key);
    }
}
