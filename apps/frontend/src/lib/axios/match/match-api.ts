'use server';
import { publicInstance } from '../axios-instance';
import { MatchError } from '../error/match-error';

export const matchFetchApi = async () => {
    try {
        const res = await publicInstance({
            url: '/matches',
            method: 'get',
        });

        return { data: res.data, status: res.status };
    } catch (error) {
        console.error(error);

        if (error instanceof MatchError) return error.response;
    }
};

export const matchFetchByTeamApi = async (team: number) => {
    try {
        const res = await publicInstance({
            url: `/matches/team/${team}`,
            method: 'get',
        });

        return { data: res.data, status: res.status };
    } catch (error) {
        console.error(error);

        if (error instanceof MatchError) return error.response;
    }
};

export const matchFetchByLeague = async (league: number) => {
    try {
        const res = await publicInstance({
            url: `/matches/league/${league}`,
            method: 'get',
        });

        return { data: res.data, status: res.status };
    } catch (error) {
        console.error(error);

        if (error instanceof MatchError) return error.response;
    }
};
