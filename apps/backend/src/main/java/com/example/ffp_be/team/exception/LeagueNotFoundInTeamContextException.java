package com.example.ffp_be.team.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class LeagueNotFoundInTeamContextException extends NotFoundException {
    public LeagueNotFoundInTeamContextException() {
        super(ErrorCode.LEAGUE_NOT_FOUND);
    }
}


