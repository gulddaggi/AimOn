export class AuthError extends Error {
    response?: {
        data: unknown;
        status: number | undefined;
        headers: string;
    };
}
