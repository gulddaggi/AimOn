'use server';
import { cookies } from 'next/headers';
import { publicInstance } from '../axios-instance';
import { loginApi } from './login-api';
import { AuthError } from '../error/auth-error';

export const signInApi = async (
    nickname: string,
    username: string,
    password: string
) => {
    try {
        const res = await publicInstance({
            url: '/auth/join',
            method: 'post',
            data: {
                nickname: nickname,
                username: username,
                password: password,
            },
        });

        const cookiesStore = await cookies();

        if (res.status === 201) {
            if (res.data.data.accessToken === undefined) {
                const temp = await loginApi(username, password);
                return { status: 201, data: temp?.data };
            } else {
                if (cookiesStore.has('accessToken'))
                    cookiesStore.delete('accessToken');
                if (cookiesStore.has('refreshToken'))
                    cookiesStore.delete('refreshToken');
                cookiesStore.set('accessToken', res.data.data.accessToken);
                cookiesStore.set('refreshToken', res.data.data.refreshToken);
            }
        }

        return { status: res.status, data: res.data };
    } catch (error) {
        console.error(error);
        console.log((error as AuthError).response);

        if (error instanceof AuthError) return error.response;
    }
};
