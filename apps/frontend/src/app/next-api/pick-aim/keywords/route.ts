import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';
import { isAxiosError } from 'axios';

export async function GET() {
    try {
        const res = await privateInstance.get('/pick-aim/keywords');
        return NextResponse.json(res.data, { status: 200 });
    } catch (error: unknown) {
        if (isAxiosError(error)) {
            const status = error.response?.status ?? 500;
            const data = error.response?.data ?? {
                message: 'Failed to fetch keywords',
            };
            return NextResponse.json(data, { status });
        }
        return NextResponse.json(
            { message: 'Failed to fetch keywords' },
            { status: 500 }
        );
    }
}
