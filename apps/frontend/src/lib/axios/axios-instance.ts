'use server';
import { cookies } from 'next/headers';
import axios from 'axios';

// 비로그인
export const publicInstance = axios.create({
    baseURL: 'https://www.aim-on.store/api',
    timeout: 10000,
});

// 로그인
export const privateInstance = axios.create({
    baseURL: 'https://www.aim-on.store/api',
    timeout: 10000,
});

// 챗봇
export const chatbotInstance = axios.create({
    baseURL: 'https://www.aim-on.store/ai/',
    headers: { 'Content-Type': 'application/json' },
    timeout: 60000,
});

// 입문가이드(목데이터)
export const guideInstance = axios.create({
    baseURL: 'http://www.aim-on.store/next-api/mock/chatbot-guide',
    timeout: 60000,
});

// 토큰용 인터셉터
privateInstance.interceptors.request.use(config => {
    return cookies().then(value => {
        const token: string | undefined = value.get('accessToken')?.value;
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    });
});

privateInstance.interceptors.response.use(
    response => response,
    async error => {
        if (error.response?.status === 401) {
            const cookiesStore = await cookies();

            const refrechToken = cookiesStore.get('refreshToken');

            try {
                const res = await publicInstance.post('/auth/refresh', {
                    refreshToken: refrechToken?.value,
                });

                const newToken = res.data.data.accessToken;

                cookiesStore.delete('accessToken');
                cookiesStore.set('accessToken', newToken);

                return privateInstance.request(error.config);
            } catch (refreshError) {
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);
