import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';
import { isAxiosError } from 'axios';

type RouteContext = { params: Promise<{ id: string }> };

export async function GET(req: Request, context: RouteContext) {
    try {
        const { id } = await context.params;
        const res = await privateInstance.get(`/teams/${id}`);
        return NextResponse.json(res.data, { status: 200 });
    } catch (error: unknown) {
        if (isAxiosError(error)) {
            const status = error.response?.status ?? 500;
            const data = error.response?.data ?? {
                message: 'Failed to fetch team',
            };
            return NextResponse.json(data, { status });
        }
        return NextResponse.json(
            { message: 'Failed to fetch team' },
            { status: 500 }
        );
    }
}
