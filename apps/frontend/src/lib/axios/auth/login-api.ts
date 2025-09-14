'use server';
import { cookies } from 'next/headers';
import { publicInstance } from '../axios-instance';
import { AuthError } from '../error/auth-error';

export const loginApi = async (username: string, password: string) => {
    try {
        const res = await publicInstance({
            url: '/auth/login',
            method: 'post',
            data: {
                username: username,
                password: password,
            },
        });

        const cookiesStore = await cookies();

        if (res.status === 200) {
            if (cookiesStore.has('accessToken'))
                cookiesStore.delete('accessToken');
            if (cookiesStore.has('refreshToken'))
                cookiesStore.delete('refreshToken');
            cookiesStore.set('accessToken', res.data.data.accessToken);
            cookiesStore.set('refreshToken', res.data.data.refreshToken);
        }

        return { status: res.status, data: res.data };
    } catch (error) {
        console.error(error);

        if (error instanceof AuthError) return error.response;
    }
};
