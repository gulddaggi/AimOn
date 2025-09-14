'use client';

import IconButton from '../atoms/buttons/icon-button';
import { ImageElement } from '@/types/image';
import googleIconOrigin from '@/resources/login/google-login.svg';
import naverIconOrigin from '@/resources/login/naver-login.svg';
import kakaoIconOrigin from '@/resources/login/kakao-login.svg';
import PlainTextBlock from '../atoms/text/plain-text-block';

const googleIcon: ImageElement = {
    src: googleIconOrigin.src,
    width: 32,
    height: 32,
};

const naverIcon: ImageElement = {
    src: naverIconOrigin.src,
    width: 32,
    height: 32,
};

const kakaoIcon: ImageElement = {
    src: kakaoIconOrigin.src,
    width: 32,
    height: 32,
};

export default function OauthButtons() {
    return (
        <div className="oauthButtons">
            <PlainTextBlock className="oauth" text="간편 로그인" />
            <IconButton
                className="google"
                image={googleIcon}
                alt="구글로 로그인"
                func={() => {
                    console.log('구글로 로그인');
                }}
            />
            <IconButton
                className="naver"
                image={naverIcon}
                alt="네이버로 로그인"
                func={() => {
                    console.log('네이버로 로그인');
                }}
            />
            <IconButton
                className="kakao"
                image={kakaoIcon}
                alt="카카오로 로그인"
                func={() => {
                    console.log('카카오로 로그인');
                }}
            />
        </div>
    );
}
