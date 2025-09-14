// 텍스트 검증 컴포넌트
'use client';

import React from 'react';

// 검증할 텍스트, 정규식, 출력문 필요
export default function TextValid(props: {
    className: string;
    isValidate: boolean;
    alert: string;
}) {
    // 검증 결과에 따라 지정된 출력문 출력
    return (
        <>
            {props.isValidate ? (
                <span className="textValidator valid">사용 가능합니다.</span>
            ) : (
                <span className={`textValidator ${props.className}`}>
                    {props.alert}
                </span>
            )}
        </>
    );
}
