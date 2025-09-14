export class GameError extends Error {
    response?: {
        data: unknown;
        status: number | undefined;
        headers: string;
    };
}
