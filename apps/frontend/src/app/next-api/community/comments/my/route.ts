import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';

export async function GET() {
    try {
        const res = await privateInstance.get('/comments/my');
        return NextResponse.json(res.data?.data ?? res.data ?? []);
    } catch {
        return NextResponse.json([], { status: 200 });
    }
}
