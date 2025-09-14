import { NextRequest, NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';

export async function GET(req: NextRequest) {
    const { searchParams } = new URL(req.url);
    const key = searchParams.get('key');
    if (!key)
        return NextResponse.json(
            { message: 'key is required' },
            { status: 400 }
        );
    try {
        const res = await privateInstance.get('/s3/profile/download-url', {
            params: { key },
        });
        const data = res.data?.data ?? res.data;
        const url = typeof data === 'string' ? data : data.url;
        return NextResponse.json({ url });
    } catch {
        return NextResponse.json(
            { message: 'failed to get download url' },
            { status: 500 }
        );
    }
}
