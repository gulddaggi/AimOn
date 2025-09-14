import { NextResponse } from 'next/server';

export async function GET() {
    // Mock API is deprecated. Return 410 Gone to avoid accidental usage.
    return NextResponse.json({ message: 'Mock API removed' }, { status: 410 });
}
