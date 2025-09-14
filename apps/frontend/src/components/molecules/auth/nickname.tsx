'use client';

import TextValidator from '@/components/atoms/inputs/text-valid';
import TextInput from '../../atoms/inputs/text-input';
import { useAppSelector, useAppDispatch } from '@/lib/redux/hooks';
import { textValidUpdate } from '@/lib/redux/feature/input/input-text-slice';
import React, { useEffect } from 'react';

// 닉네임 검증용 정규식
const nicknameRegex = /^[a-zA-Z0-9ㄱ-ㅎ가-힣_]{2,}$/;

export default function Nickname(props: { validatorOn: boolean }) {
    const dispatch = useAppDispatch();

    const input = useAppSelector(state => state.inputText.value);
    const isValidate = useAppSelector(state => state.inputText.valid);

    useEffect(() => {
        dispatch(textValidUpdate(nicknameRegex.test(input)));
    }, [input, dispatch]);

    return (
        <div className="authMole">
            <TextInput
                className="authInput nickname"
                name="nickname"
                type="nickname"
                placeholder="닉네임"
            />
            {props.validatorOn ? (
                <TextValidator
                    className="authVal nickname"
                    isValidate={isValidate}
                    alert="유효하지 않은 닉네임입니다."
                />
            ) : null}
        </div>
    );
}
