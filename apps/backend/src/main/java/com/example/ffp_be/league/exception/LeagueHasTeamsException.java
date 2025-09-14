package com.example.ffp_be.league.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class LeagueHasTeamsException extends CustomException {

    public LeagueHasTeamsException() {
        super(ErrorCode.LEAGUE_HAS_TEAMS);
    }
}


