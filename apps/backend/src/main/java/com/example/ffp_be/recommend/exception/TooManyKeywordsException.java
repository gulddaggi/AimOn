package com.example.ffp_be.recommend.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class TooManyKeywordsException extends CustomException {

    public TooManyKeywordsException(int size) {
        super(ErrorCode.PICK_KEYWORDS_TOO_MANY);
    }
}


