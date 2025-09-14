package com.example.ffp_be.clip.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class InvalidYoutubeUrlException extends CustomException {

    public InvalidYoutubeUrlException() {
        super(ErrorCode.INVALID_YOUTUBE_URL);
    }
}


