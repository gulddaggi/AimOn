import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';
import { isAxiosError } from 'axios';

export async function GET() {
    try {
        const res = await privateInstance.get('/likes/teams/me');
        return NextResponse.json(res.data, { status: 200 });
    } catch (error: unknown) {
        if (isAxiosError(error)) {
            const status = error.response?.status ?? 500;
            const data = error.response?.data ?? {
                message: 'Failed to fetch my liked teams',
            };
            return NextResponse.json(data, { status });
        }
        return NextResponse.json(
            { message: 'Failed to fetch my liked teams' },
            { status: 500 }
        );
    }
}
