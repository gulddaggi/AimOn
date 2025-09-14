package com.example.ffp_be.auth.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class LoginFailedException extends CustomException {

    public LoginFailedException() {
        super(ErrorCode.LOGIN_FAILED);
    }

    public LoginFailedException(String message) {
        super(ErrorCode.LOGIN_FAILED);
    }
}
