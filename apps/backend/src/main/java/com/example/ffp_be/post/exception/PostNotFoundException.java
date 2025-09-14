package com.example.ffp_be.post.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class PostNotFoundException extends NotFoundException {

    public PostNotFoundException(Long postId) {
        super(ErrorCode.POST_NOT_FOUND);
    }
}


