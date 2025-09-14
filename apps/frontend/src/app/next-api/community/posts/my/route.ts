import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';

export async function GET() {
    try {
        const res = await privateInstance.get('/posts/my');
        return NextResponse.json(res.data?.data ?? res.data ?? []);
    } catch (err: unknown) {
        const status =
            (err as { response?: { status?: number } })?.response?.status ??
            500;
        return NextResponse.json({ error: true }, { status });
    }
}
