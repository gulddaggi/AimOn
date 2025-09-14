'use server';
import { guideInstance } from '../axios-instance';
import { GuideError } from '../error/guide-error';

export const guideApi = async (elements: string) => {
    try {
        const res = await guideInstance({
            url: `/${elements}`,
            method: 'get',
        });

        if (res.status === 200) {
            return { status: res.status, data: res.data };
        }
    } catch (error) {
        console.error(error);

        if (error instanceof GuideError) return error.response;
    }
};
