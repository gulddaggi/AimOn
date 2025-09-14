'use client';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useAppSelector, useAppDispatch } from '@/lib/redux/hooks';
import { setIsSucceeded } from '@/lib/redux/feature/auth/auth-slice';
import Toast from '../toast';

export default function LoginToast() {
    const route = usePathname();

    const dispatch = useAppDispatch();

    const [isUpdated, setIsUpdated] = useState(false);
    const [message, setMessage] = useState<string | undefined>(undefined);
    const isSucceeded = useAppSelector(state => state.auth.isSucceeded);

    useEffect(() => {
        if (isSucceeded !== undefined) setIsUpdated(true);
    }, [isSucceeded]);

    const reset = () => {
        dispatch(setIsSucceeded(undefined));
        setMessage(undefined);
        setIsUpdated(false);
    };

    useEffect(() => {
        if (isUpdated) {
            if (isSucceeded === 'login' && route === '/') {
                setMessage('환영합니다');
            } else if (isSucceeded === 'fail') {
                setMessage('로그인에 실패하였습니다');
            }
        }
    }, [route, isUpdated, isSucceeded]);

    return (
        <>
            {message !== undefined ? (
                <Toast message={message} onDone={reset} />
            ) : null}
        </>
    );
}
