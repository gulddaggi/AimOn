package com.example.ffp_be.recommend.exception;

import com.example.ffp_be.common.exception.ErrorCode;
import com.example.ffp_be.common.exception.NotFoundException;

public class PickKeywordMetaNotFoundException extends NotFoundException {

    public PickKeywordMetaNotFoundException() {
        super(ErrorCode.PICK_KEYWORD_META_NOT_FOUND);
    }
}


