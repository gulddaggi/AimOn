package com.example.ffp_be.like.exception;

import com.example.ffp_be.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class UserRequiredException extends CustomException {

    public UserRequiredException() {
        super(HttpStatus.UNAUTHORIZED, "COMMON_001", "로그인이 필요합니다.");
    }
}


