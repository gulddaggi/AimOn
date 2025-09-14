package com.example.ffp_be.s3.service;

public interface S3Service {
    public String generateUploadUrl(String key, String contentType);

    public String generateDownloadUrl(String key);
}
