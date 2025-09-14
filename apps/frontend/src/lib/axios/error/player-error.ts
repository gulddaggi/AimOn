export class PlayerError extends Error {
    response?: {
        data: unknown;
        status: number | undefined;
        headers: string;
    };
}
