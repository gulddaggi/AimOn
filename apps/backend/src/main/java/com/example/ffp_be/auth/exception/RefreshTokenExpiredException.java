package com.example.ffp_be.auth.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class RefreshTokenExpiredException extends CustomException {

    public RefreshTokenExpiredException() {
        super(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    public RefreshTokenExpiredException(String message) {
        super(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }
}
