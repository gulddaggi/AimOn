import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';

type RouteContext = { params: Promise<{ postId: string }> };

export async function GET(_req: Request, context: RouteContext) {
    const { postId } = await context.params;
    const res = await privateInstance.get(`/comments/post/${postId}`);
    // 백엔드가 작성자 프로필 키를 내려줄 수 있으므로 그대로 전달하고,
    // 클라이언트에서 presigned 변환 훅을 사용해 처리한다.
    return NextResponse.json(res.data ?? []);
}
