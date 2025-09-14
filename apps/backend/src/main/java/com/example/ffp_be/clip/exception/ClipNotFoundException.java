package com.example.ffp_be.clip.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class ClipNotFoundException extends NotFoundException {

    public ClipNotFoundException() {
        super(ErrorCode.CLIP_NOT_FOUND);
    }
}


