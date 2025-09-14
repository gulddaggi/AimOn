export class GuideError extends Error {
    response?: {
        data: unknown;
        status: number | undefined;
        headers: string;
    };
}
