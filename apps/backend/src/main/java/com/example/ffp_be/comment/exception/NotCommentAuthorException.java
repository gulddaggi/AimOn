package com.example.ffp_be.comment.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class NotCommentAuthorException extends CustomException {

    public NotCommentAuthorException() {
        super(ErrorCode.NOT_COMMENT_AUTHOR);
    }
}



