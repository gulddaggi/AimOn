package com.example.ffp_be.team.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class GameNotFoundInTeamContextException extends NotFoundException {
    public GameNotFoundInTeamContextException() {
        super(ErrorCode.GAME_NOT_FOUND);
    }
}


