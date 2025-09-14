import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';

type RouteContext = { params: Promise<{ postId: string }> };

export async function POST(_req: Request, context: RouteContext) {
    const { postId } = await context.params;
    const res = await privateInstance.post(`/likes/posts/${postId}`);
    return NextResponse.json(res.data ?? {});
}
