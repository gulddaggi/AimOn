package com.example.ffp_be.news.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class NaverApiException extends CustomException {

    public NaverApiException(String message) {
        super(ErrorCode.NAVER_API_ERROR);
    }
}


