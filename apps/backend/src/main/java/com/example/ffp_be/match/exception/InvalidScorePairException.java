package com.example.ffp_be.match.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class InvalidScorePairException extends CustomException {

    public InvalidScorePairException() {
        super(ErrorCode.MATCH_INVALID_SCORE_PAIR);
    }
}


