import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';

type BackendLikedPost = { postId: number };
type RouteContext = { params: Promise<{ id: string }> };

export async function GET(_req: Request, context: RouteContext) {
    const { id } = await context.params;
    const res = await privateInstance.get(`/posts/${id}`);
    let liked = false;
    try {
        const likedRes = await privateInstance.get('/likes/myposts');
        let likedList: BackendLikedPost[] = [];
        if (Array.isArray(likedRes.data)) {
            likedList = likedRes.data as BackendLikedPost[];
        } else if (Array.isArray(likedRes.data?.data)) {
            likedList = likedRes.data.data as BackendLikedPost[];
        }
        liked = likedList.some(l => String(l.postId) === String(id));
    } catch {}
    const base = res.data?.data ?? res.data ?? {};
    return NextResponse.json({ ...base, liked });
}

export async function PUT(req: Request, context: RouteContext) {
    const { id } = await context.params;
    const body = await req.json();
    const res = await privateInstance.put(`/posts/${id}`, body, {
        headers: { 'Content-Type': 'application/json' },
    });
    return NextResponse.json(res.data ?? {});
}

export async function DELETE(_req: Request, context: RouteContext) {
    const { id } = await context.params;
    const res = await privateInstance.delete(`/posts/${id}`);
    return NextResponse.json(res.data ?? {});
}
