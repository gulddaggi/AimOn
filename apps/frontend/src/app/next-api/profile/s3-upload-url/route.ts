import { NextRequest, NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';

export async function GET(req: NextRequest) {
    const { searchParams } = new URL(req.url);
    const userId = searchParams.get('userId');
    const contentType = searchParams.get('contentType');
    if (!userId || !contentType) {
        return NextResponse.json(
            { message: 'userId and contentType are required' },
            { status: 400 }
        );
    }
    try {
        const res = await privateInstance.get('/s3/profile/upload-url', {
            params: { userId, contentType },
        });
        return NextResponse.json(res.data?.data ?? res.data);
    } catch {
        return NextResponse.json(
            { message: 'failed to get upload url' },
            { status: 500 }
        );
    }
}
