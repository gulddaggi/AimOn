package com.example.ffp_be.league.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class LeagueHasMatchesException extends CustomException {

    public LeagueHasMatchesException() {
        super(ErrorCode.LEAGUE_HAS_MATCHES);
    }
}


