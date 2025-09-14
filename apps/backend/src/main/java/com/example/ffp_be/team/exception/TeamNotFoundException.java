package com.example.ffp_be.team.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class TeamNotFoundException extends NotFoundException {
    public TeamNotFoundException() {
        super(ErrorCode.TEAM_NOT_FOUND);
    }
}


