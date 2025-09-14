'use client';

import TextValid from '@/components/atoms/inputs/text-valid';
import TextInput from '../../atoms/inputs/text-input';
import { useAppSelector, useAppDispatch } from '@/lib/redux/hooks';
import { emailValidUpdate } from '@/lib/redux/feature/input/input-email-slice';
import React, { useEffect } from 'react';

// 이메일 검증용 정규식
const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

export default function Email(props: { validatorOn: boolean }) {
    const dispatch = useAppDispatch();

    const input = useAppSelector(state => state.inputEmail.value);
    const isValidate = useAppSelector(state => state.inputEmail.valid);

    useEffect(() => {
        dispatch(emailValidUpdate(emailRegex.test(input)));
    }, [input, dispatch]);

    return (
        <div className="authMole">
            <TextInput
                className="authInput email"
                name="email"
                type="email"
                placeholder="이메일"
            />
            {props.validatorOn ? (
                <TextValid
                    className="authVal email"
                    isValidate={isValidate}
                    alert="유효하지 않은 이메일입니다."
                />
            ) : null}
        </div>
    );
}
