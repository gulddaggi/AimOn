'use server';
import { publicInstance } from '../axios-instance';
import { GameError } from '../error/game-error';

export const gameApi = async () => {
    try {
        const res = await publicInstance({
            url: '/games',
            method: 'get',
        });

        return { data: res.data, status: res.status };
    } catch (error) {
        console.error(error);

        if (error instanceof GameError) return error.response;
    }
};
