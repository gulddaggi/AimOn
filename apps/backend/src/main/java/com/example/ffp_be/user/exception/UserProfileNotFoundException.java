package com.example.ffp_be.user.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class UserProfileNotFoundException extends NotFoundException {

    public UserProfileNotFoundException(Long userId) {
        super(ErrorCode.USER_PROFILE_NOT_FOUND);
    }

    public UserProfileNotFoundException(String nickname) {
        super(ErrorCode.USER_PROFILE_NOT_FOUND);
    }
}
