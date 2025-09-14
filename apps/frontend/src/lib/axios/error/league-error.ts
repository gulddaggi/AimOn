export class LeagueError extends Error {
    response?: {
        data: unknown;
        status: number | undefined;
        headers: string;
    };
}
