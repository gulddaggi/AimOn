package com.example.ffp_be.match.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class MatchNotFoundException extends NotFoundException {

    public MatchNotFoundException() {
        super(ErrorCode.MATCH_NOT_FOUND);
    }
}


