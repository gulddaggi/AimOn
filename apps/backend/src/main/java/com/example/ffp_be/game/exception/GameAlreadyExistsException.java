package com.example.ffp_be.game.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class GameAlreadyExistsException extends CustomException {

    public GameAlreadyExistsException() {
        super(ErrorCode.DUPLICATED_GAME);
    }
}


