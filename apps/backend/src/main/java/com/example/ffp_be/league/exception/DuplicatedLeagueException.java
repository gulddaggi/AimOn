package com.example.ffp_be.league.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class DuplicatedLeagueException extends CustomException {

    public DuplicatedLeagueException(String name) {
        super(ErrorCode.DUPLICATED_LEAGUE);
    }
}


