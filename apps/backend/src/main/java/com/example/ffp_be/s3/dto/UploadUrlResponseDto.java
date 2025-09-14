package com.example.ffp_be.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadUrlResponseDto {
    private String uploadUrl;
    private String key;
}
