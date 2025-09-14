import { NextRequest, NextResponse } from 'next/server';
import guideData from '@/mock/guide.json';

export async function GET(
    request: NextRequest,
    { params }: { params: Promise<{ elements: string }> }
) {
    try {
        const { elements } = await params;
        const result = await processQuery(elements);
        return NextResponse.json(result);
    } catch (error) {
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}

async function processQuery(elements: string) {
    let result: {
        outline?: string;
        keys?: Array<string>;
        teamIcon?: string;
        players?: {
            url: string;
            name: string;
            handle: string;
            country: string;
        }[];
    };
    switch (elements) {
        case 'init':
            result = {
                outline: guideData['개요'],
                keys: Object.keys(guideData['세부 정보']).filter(
                    key => key !== '개요'
                ),
            };
            break;
        case 'E스포츠':
            result = {
                outline: guideData['세부 정보']['E스포츠']['개요'],
                keys: Object.keys(
                    guideData['세부 정보']['E스포츠']['세부 정보']
                ),
            };
            break;
        case '국제리그':
            result = {
                keys: Object.keys(
                    guideData['세부 정보']['E스포츠']['세부 정보']['국제리그']
                ),
            };
            break;
        case 'Pacific':
            result = {
                outline:
                    guideData['세부 정보']['E스포츠']['세부 정보']['국제리그'][
                        'Pacific'
                    ]['개요'],
                keys: Object.keys(
                    guideData['세부 정보']['E스포츠']['세부 정보']['국제리그'][
                        'Pacific'
                    ]['세부 사항']['참가팀']
                ).filter(key => key !== '개요'),
            };
            break;
        case 'Gen.G':
            result = {
                outline:
                    guideData['세부 정보']['E스포츠']['세부 정보']['국제리그'][
                        'Pacific'
                    ]['세부 사항']['참가팀']['Gen.G']['개요'],
                keys: Object.keys(
                    guideData['세부 정보']['E스포츠']['세부 정보']['국제리그'][
                        'Pacific'
                    ]['세부 사항']['참가팀']['Gen.G']
                ).filter(key => key !== '개요' && key !== 'url'),
                teamIcon:
                    guideData['세부 정보']['E스포츠']['세부 정보']['국제리그'][
                        'Pacific'
                    ]['세부 사항']['참가팀']['Gen.G']['url'],
            };
            break;
        case '로스터':
            result = {
                players:
                    guideData['세부 정보']['E스포츠']['세부 정보']['국제리그'][
                        'Pacific'
                    ]['세부 사항']['참가팀']['Gen.G']['로스터'],
            };
            break;
        default:
            result = {
                outline: '개발 중입니다.',
                keys: [],
            };
    }

    return result;
}
