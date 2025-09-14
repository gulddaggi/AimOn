import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';
import { isAxiosError } from 'axios';

type CandidatesRequestBody = {
    gameId: number;
    leagueId: number;
    keywords: string[];
};

export async function POST(req: Request) {
    try {
        const body = (await req.json()) as CandidatesRequestBody;
        const res = await privateInstance.post('/pick-aim/candidates', body);
        return NextResponse.json(res.data, { status: 200 });
    } catch (error: unknown) {
        if (isAxiosError(error)) {
            const status = error.response?.status ?? 500;
            const data = error.response?.data ?? {
                message: 'Failed to fetch candidates',
            };
            return NextResponse.json(data, { status });
        }
        return NextResponse.json(
            { message: 'Failed to fetch candidates' },
            { status: 500 }
        );
    }
}
