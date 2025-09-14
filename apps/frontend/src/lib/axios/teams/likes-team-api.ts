'use server';
import { privateInstance } from '../axios-instance';
import { TeamsError } from '../error/teams-error';

export const likesTeamFetchApi = async () => {
    try {
        const res = await privateInstance({
            url: '/likes/teams/me',
            method: 'get',
        });

        return { data: res.data, status: res.status };
    } catch (error) {
        console.error(error);

        if (error instanceof TeamsError) return error.response;
    }
};
