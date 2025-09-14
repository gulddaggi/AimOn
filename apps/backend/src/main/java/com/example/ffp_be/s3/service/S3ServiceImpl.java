package com.example.ffp_be.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String generateUploadUrl(String key, String contentType) {
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration);
        request.addRequestParameter("Content-Type", contentType);
        URL url = amazonS3.generatePresignedUrl(request);
        return url.toString();
    }

    public String generateDownloadUrl(String key) {
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        URL url = amazonS3.generatePresignedUrl(request);
        return url.toString();
    }
}
