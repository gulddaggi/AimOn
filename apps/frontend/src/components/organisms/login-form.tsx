'use client';
import { useRouter } from 'next/navigation';
import { useState, useEffect } from 'react';
import { useAppSelector, useAppDispatch } from '@/lib/redux/hooks';
import {
    emailUpdate,
    emailValidUpdate,
} from '@/lib/redux/feature/input/input-email-slice';
import {
    passwordUpdate,
    passwordValidUpdate,
} from '@/lib/redux/feature/input/input-password-slice';
import { login } from '@/lib/redux/feature/auth/auth-slice';
import TextButton from '../atoms/buttons/text-button';
import Email from '../molecules/auth/email';
import Password from '../molecules/auth/password';

export default function LoginForm() {
    const dispatch = useAppDispatch();
    const [isValid, setIsValid] = useState(false);
    const [isReseted, setIsReseted] = useState(false);

    useEffect(() => {
        dispatch(emailUpdate(''));
        dispatch(emailValidUpdate(false));
        dispatch(passwordUpdate(''));
        dispatch(passwordValidUpdate(false));
        setIsReseted(true);
    }, [dispatch]);

    const emailValid = useAppSelector(state => state.inputEmail.valid);
    const passwordValid = useAppSelector(state => state.inputPassword.valid);

    const loginCredential = {
        email: useAppSelector(state => state.inputEmail.value),
        password: useAppSelector(state => state.inputPassword.value),
    };

    const isSuccess = useAppSelector(state => state.auth.isAuthorized);
    const route = useRouter();

    useEffect(() => {
        if (isReseted) setIsValid(emailValid && passwordValid);
    }, [isReseted, emailValid, passwordValid]);

    useEffect(() => {
        if (isSuccess) {
            route.push('/');
        }
    }, [isSuccess, route]);

    // 테스트용
    useEffect(() => {
        if (!isValid)
            setIsValid(
                loginCredential.email === 'string' &&
                    loginCredential.password === 'string'
            );
    }, [isValid, loginCredential.email, loginCredential.password]);
    return (
        <div className="authForm loginForm">
            <Email validatorOn={false} />
            <Password validatorOn={false} />
            <TextButton
                className={`authBtn ${isValid} login`}
                text="로그인"
                func={async () => {
                    if (isValid) dispatch(login(loginCredential));
                }}
            />
        </div>
    );
}
