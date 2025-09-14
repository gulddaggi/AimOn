'use client';
import { useRouter } from 'next/navigation';
import { useState, useEffect } from 'react';
import { useAppSelector, useAppDispatch } from '@/lib/redux/hooks';
import {
    textUpdate,
    textValidUpdate,
} from '@/lib/redux/feature/input/input-text-slice';
import {
    emailUpdate,
    emailValidUpdate,
} from '@/lib/redux/feature/input/input-email-slice';
import {
    passwordUpdate,
    passwordValidUpdate,
} from '@/lib/redux/feature/input/input-password-slice';
import { signIn } from '@/lib/redux/feature/auth/auth-slice';
import TextButton from '../atoms/buttons/text-button';
import Email from '../molecules/auth/email';
import Nickname from '../molecules/auth/nickname';
import Password from '../molecules/auth/password';

export default function SignInForm() {
    const dispatch = useAppDispatch();
    const [isValid, setIsValid] = useState(false);
    const [isReseted, setIsReseted] = useState(false);

    useEffect(() => {
        dispatch(textUpdate(''));
        dispatch(textValidUpdate(false));
        dispatch(emailUpdate(''));
        dispatch(emailValidUpdate(false));
        dispatch(passwordUpdate(''));
        dispatch(passwordValidUpdate(false));
        setIsReseted(true);
    }, [dispatch]);

    const nicknameValid = useAppSelector(state => state.inputText.valid);
    const emailValid = useAppSelector(state => state.inputEmail.valid);
    const passwordValid = useAppSelector(state => state.inputPassword.valid);

    const signInCredential = {
        nickname: useAppSelector(state => state.inputText.value),
        email: useAppSelector(state => state.inputEmail.value),
        password: useAppSelector(state => state.inputPassword.value),
    };

    const isSuccess = useAppSelector(state => state.auth.isSucceeded);
    const route = useRouter();

    useEffect(() => {
        if (isReseted) setIsValid(nicknameValid && emailValid && passwordValid);
    }, [isReseted, nicknameValid, emailValid, passwordValid]);

    useEffect(() => {
        if (isSuccess === 'signIn') {
            route.push('/');
        }
    }, [isSuccess, route]);

    return (
        <div className="authForm signInForm">
            <Nickname validatorOn={true} />
            <Email validatorOn={true} />
            <Password validatorOn={true} />
            <TextButton
                className={`authBtn ${isValid} signIn`}
                text="회원가입"
                func={async () => {
                    if (isValid) dispatch(signIn(signInCredential));
                }}
            />
        </div>
    );
}
