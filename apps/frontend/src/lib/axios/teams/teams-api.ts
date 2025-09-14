'use server';
import { publicInstance } from '../axios-instance';
import { TeamsError } from '../error/teams-error';

export const teamFetchApi = async () => {
    try {
        const res = await publicInstance({
            url: '/teams',
            method: 'get',
        });

        return { data: res.data, status: res.status };
    } catch (error) {
        console.error(error);

        if (error instanceof TeamsError) return error.response;
    }
};

export const teamFetchByName = async (name: string) => {
    try {
        const res = await publicInstance({
            url: `/teams/search?name=${name}`,
            method: 'get',
        });

        return { data: res.data, status: res.status };
    } catch (error) {
        console.error(error);

        if (error instanceof TeamsError) return error.response;
    }
};

export const teamFetchByLeagueApi = async (leagueId: number) => {
    try {
        const res = await publicInstance({
            url: `/teams/league/${leagueId}`,
            method: 'get',
        });

        return { data: res.data, status: res.status };
    } catch (error) {
        console.error(error);

        if (error instanceof TeamsError) return error.response;
    }
};
