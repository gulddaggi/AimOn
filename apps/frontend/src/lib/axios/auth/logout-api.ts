'use server';
import { cookies } from 'next/headers';
import { privateInstance } from '../axios-instance';
import { AuthError } from '../error/auth-error';

export const logoutApi = async () => {
    try {
        const cookiesStore = await cookies();

        const refrechToken = cookiesStore.get('refreshToken')?.value;

        const res = await privateInstance({
            url: '/auth/logout',
            method: 'post',
            data: {
                refreshToken: refrechToken,
            },
        });

        if (res.status === 200) {
            cookiesStore.delete('accessToken');
            cookiesStore.delete('refreshToken');
        }

        return { status: res.status, data: res.data };
    } catch (error) {
        console.error(error);

        if (error instanceof AuthError) return error.response;
    }
};
