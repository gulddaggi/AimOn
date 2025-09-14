package com.example.ffp_be.user.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class DuplicatedNicknameException extends CustomException {

    public DuplicatedNicknameException(String nickname) {
        super(ErrorCode.DUPLICATED_NICKNAME);
    }
}
