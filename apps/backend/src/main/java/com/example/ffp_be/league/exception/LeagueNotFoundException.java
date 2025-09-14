package com.example.ffp_be.league.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class LeagueNotFoundException extends NotFoundException {

    public LeagueNotFoundException() {
        super(ErrorCode.LEAGUE_NOT_FOUND);
    }
}


