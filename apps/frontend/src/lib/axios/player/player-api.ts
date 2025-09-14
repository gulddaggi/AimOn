'use server';
import { publicInstance } from '../axios-instance';
import { PlayerError } from '../error/player-error';

export const playerFetchByTeamApi = async (team: number) => {
    try {
        const res = await publicInstance({
            url: `/players/team/${team}`,
            method: 'get',
        });

        return { data: res.data, status: res.status };
    } catch (error) {
        console.error(error);

        if (error instanceof PlayerError) return error.response;
    }
};
