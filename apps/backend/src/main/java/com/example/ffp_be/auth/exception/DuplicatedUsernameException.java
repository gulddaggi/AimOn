package com.example.ffp_be.auth.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class DuplicatedUsernameException extends CustomException {

    public DuplicatedUsernameException() {
        super(ErrorCode.DUPLICATED_USERNAME);
    }

    public DuplicatedUsernameException(String message) {
        super(ErrorCode.DUPLICATED_USERNAME);
    }
}
