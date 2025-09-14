import { NextRequest, NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';

export async function POST(req: NextRequest) {
    const body = await req.json();
    // 백엔드 명세: parentCommentId가 null(댓글) 또는 부모 id(대댓글)
    const payload = {
        postId: body.postId,
        parentCommentId: body.parentCommentId ?? null,
        content: body.content,
    };
    const res = await privateInstance.post('/comments', payload, {
        headers: { 'Content-Type': 'application/json' },
    });
    return NextResponse.json(res.data ?? {});
}
