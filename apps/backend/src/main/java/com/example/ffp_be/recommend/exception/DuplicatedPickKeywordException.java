package com.example.ffp_be.recommend.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class DuplicatedPickKeywordException extends CustomException {

    public DuplicatedPickKeywordException(String key) {
        super(ErrorCode.DUPLICATED_PICK_KEYWORD);
    }
}


