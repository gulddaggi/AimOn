package com.example.ffp_be.news.exception;

import com.example.ffp_be.common.exception.CustomException;
import com.example.ffp_be.common.exception.ErrorCode;

public class NaverCredentialsMissingException extends CustomException {

    public NaverCredentialsMissingException() {
        super(ErrorCode.NAVER_CREDENTIALS_MISSING);
    }
}


