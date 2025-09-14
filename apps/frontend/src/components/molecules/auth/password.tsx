'use client';
import TextValid from '@/components/atoms/inputs/text-valid';
import TextInput from '../../atoms/inputs/text-input';
import { useAppSelector, useAppDispatch } from '@/lib/redux/hooks';
import { passwordValidUpdate } from '@/lib/redux/feature/input/input-password-slice';
import React, { useState, useEffect } from 'react';
import IconButton from '@/components/atoms/buttons/icon-button';
import visiblityIcon from '@/resources/login/visibility.svg';
import visiblityOffIcon from '@/resources/login/visibility-off.svg';

// 비밀번호 검증용 정규식
const passwordRegex =
    /^(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?])(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]{8,15}$/;

export default function Password(props: { validatorOn: boolean }) {
    const dispatch = useAppDispatch();

    const input = useAppSelector(state => state.inputPassword.value);
    const isValidate = useAppSelector(state => state.inputPassword.valid);

    useEffect(() => {
        dispatch(passwordValidUpdate(passwordRegex.test(input)));
    }, [input, dispatch]);

    // 비밀번호 필드 값 조회 버튼 트리거
    const [isActivate, setISActivate] = useState(false);
    useEffect(() => {
        const isNull = input === '' ? true : false;
        setISActivate(!isNull);
    }, [input]);

    // 비밀번호 필드 값 출력 리랜더링 트리거
    // 해당 값이 변경되면 비밀번호 필드 값을 사용자가 볼 수 있음
    const [isReveal, setIsReveal] = useState(false);

    function reveal() {
        setIsReveal(!isReveal);
    }

    return (
        <div className="authMole">
            <div className="passwordInput">
                {isReveal ? (
                    <TextInput
                        className="authInput password"
                        name="password"
                        type="text"
                    />
                ) : (
                    <TextInput
                        className="authInput password"
                        name="password"
                        type="password"
                        placeholder="비밀번호"
                    />
                )}
                {isActivate ? (
                    isReveal ? (
                        <IconButton
                            className="invisible"
                            image={visiblityOffIcon}
                            alt="숨기기 버튼"
                            func={reveal}
                        />
                    ) : (
                        <IconButton
                            className="visible"
                            image={visiblityIcon}
                            alt="보기 버튼"
                            func={reveal}
                        />
                    )
                ) : null}
            </div>
            {props.validatorOn ? (
                <TextValid
                    className="authVal password"
                    isValidate={isValidate}
                    alert="유효하지 않은 비밀번호입니다."
                />
            ) : null}
        </div>
    );
}
