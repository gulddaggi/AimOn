package com.example.ffp_be.like.exception;

import com.example.ffp_be.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends CustomException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "COMMON_404", message);
    }
}


