package com.example.ffp_be.user.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class InvalidLevelException extends CustomException {

    public InvalidLevelException() {
        super(ErrorCode.INVALID_LEVEL);
    }
}
