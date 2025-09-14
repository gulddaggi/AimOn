package com.example.ffp_be.game.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class GameNotFoundException extends NotFoundException {

    public GameNotFoundException() {
        super(ErrorCode.GAME_NOT_FOUND);
    }
}
