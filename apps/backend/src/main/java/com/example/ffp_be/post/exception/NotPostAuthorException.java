package com.example.ffp_be.post.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class NotPostAuthorException extends CustomException {

    public NotPostAuthorException() {
        super(ErrorCode.NOT_POST_AUTHOR);
    }
}


