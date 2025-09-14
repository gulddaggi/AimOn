import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';

type RouteContext = { params: Promise<{ id: string }> };

export async function GET(_req: Request, context: RouteContext) {
    const { id } = await context.params;
    try {
        const res = await privateInstance.get(`/users/by-id/${id}`);
        return NextResponse.json(res.data?.data ?? res.data ?? {});
    } catch {
        return NextResponse.json({}, { status: 200 });
    }
}
