'use server';
import { publicInstance } from '../axios-instance';
import { LeagueError } from '../error/league-error';

export const leagueApi = async (gameId: number) => {
    try {
        const res = await publicInstance({
            url: `/leagues/game/${gameId}`,
            method: 'get',
        });

        return { data: res.data, status: res.status };
    } catch (error) {
        console.error(error);

        if (error instanceof LeagueError) return error.response;
    }
};
