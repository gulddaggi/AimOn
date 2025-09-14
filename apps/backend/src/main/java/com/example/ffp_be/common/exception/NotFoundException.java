package com.example.ffp_be.common.exception;

import org.springframework.http.HttpStatus;

public abstract class NotFoundException extends CustomException {

    protected NotFoundException(String code, String message) {
        super(HttpStatus.NOT_FOUND, code, message);
    }

    protected NotFoundException(ErrorCode errorCode) {
        super(errorCode);
        if (errorCode.getStatus() != HttpStatus.NOT_FOUND) {
            throw new IllegalArgumentException("ErrorCode status must be NOT_FOUND");
        }
    }
}
