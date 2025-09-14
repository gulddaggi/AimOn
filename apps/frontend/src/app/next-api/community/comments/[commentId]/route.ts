import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';
import { isAxiosError } from 'axios';

type RouteContext = { params: Promise<{ commentId: string }> };

export async function PUT(req: Request, context: RouteContext) {
    try {
        const { commentId } = await context.params;
        const body = await req.json().catch(() => null);
        const payload =
            typeof body === 'string'
                ? { content: body }
                : { content: body?.content };
        if (!payload.content || typeof payload.content !== 'string') {
            return NextResponse.json(
                { message: 'content is required' },
                { status: 400 }
            );
        }
        const res = await privateInstance.put(
            `/comments/${commentId}`,
            payload,
            { headers: { 'Content-Type': 'application/json' } }
        );
        return NextResponse.json(res.data ?? {}, { status: 200 });
    } catch (error: unknown) {
        if (isAxiosError(error)) {
            const status = error.response?.status ?? 500;
            const data = error.response?.data ?? {
                message: 'Failed to update comment',
            };
            return NextResponse.json(data, { status });
        }
        return NextResponse.json(
            { message: 'Failed to update comment' },
            { status: 500 }
        );
    }
}

export async function DELETE(_req: Request, context: RouteContext) {
    try {
        const { commentId } = await context.params;
        const res = await privateInstance.delete(`/comments/${commentId}`);
        return NextResponse.json(res.data ?? {}, { status: 200 });
    } catch (error: unknown) {
        if (isAxiosError(error)) {
            const status = error.response?.status ?? 500;
            const data = error.response?.data ?? {
                message: 'Failed to delete comment',
            };
            return NextResponse.json(data, { status });
        }
        return NextResponse.json(
            { message: 'Failed to delete comment' },
            { status: 500 }
        );
    }
}
