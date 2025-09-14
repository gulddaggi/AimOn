package com.example.ffp_be.player.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class PlayerNotFoundException extends NotFoundException {
    public PlayerNotFoundException() {
        super(ErrorCode.PLAYER_NOT_FOUND);
    }
}


