package com.example.ffp_be.recommend.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class InvalidKeywordException extends CustomException {

    public InvalidKeywordException(String key) {
        super(ErrorCode.INVALID_KEYWORD);
    }
}


