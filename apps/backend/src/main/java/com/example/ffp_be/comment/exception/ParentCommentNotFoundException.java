package com.example.ffp_be.comment.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class ParentCommentNotFoundException extends NotFoundException {

    public ParentCommentNotFoundException() {
        super(ErrorCode.PARENT_COMMENT_NOT_FOUND);
    }
}



