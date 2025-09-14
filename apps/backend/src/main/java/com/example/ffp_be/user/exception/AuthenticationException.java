package com.example.ffp_be.user.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class AuthenticationException extends CustomException {

    public AuthenticationException() {
        super(ErrorCode.AUTHENTICATION_ERROR);
    }
}
