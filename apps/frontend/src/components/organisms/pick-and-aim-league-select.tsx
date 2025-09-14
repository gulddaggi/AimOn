'use client';
import Image, { type StaticImageData } from 'next/image';
import TextButton from '@/components/atoms/buttons/text-button';
import resourceBack from '@/resources/pick-and-aim/resource-paa-back.svg';
import { useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';

import vctAmericas from '@/resources/pick-and-aim/logo-league-vct-americas.svg';
import vctCN from '@/resources/pick-and-aim/logo-league-vct-cn.svg';
import vctEMEA from '@/resources/pick-and-aim/logo-league-vct-emea.svg';
import vctPacific from '@/resources/pick-and-aim/logo-league-vct-pacific.svg';

import owcsAsia from '@/resources/pick-and-aim/logo-league-owcs-asia.png';
import owcsChina from '@/resources/pick-and-aim/logo-league-owcs-china.png';
import owcsNA from '@/resources/pick-and-aim/logo-league-owcs-northamerica.png';
import owcsEMEA from '@/resources/pick-and-aim/logo-league-owcs-emea.png';

type GameCode = 'VALORANT' | 'OVERWATCH2';

type LeagueCard = {
    leagueId: number;
    code: string;
    logo: StaticImageData | string; // local asset path (png/svg)
    region: string;
    time: string;
    width?: number;
    height?: number;
};

const VCT_CARDS: LeagueCard[] = [
    {
        leagueId: 1,
        code: 'PACIFIC',
        logo: vctPacific,
        region: '지역 : 아시아-태평양',
        time: '경기 시작 : 오후 05 ~ 07시',
    },
    {
        leagueId: 2,
        code: 'EMEA',
        logo: vctEMEA,
        region: '지역 : 유럽,중동, 아프리카',
        time: '경기 시작 : 오전 12 ~ 03시',
    },
    {
        leagueId: 3,
        code: 'CN',
        logo: vctCN,
        region: '지역 : 중국',
        time: '경기 시작 : 오후 06~08시',
    },
    {
        leagueId: 4,
        code: 'AMERICAS',
        logo: vctAmericas,
        region: '지역 : 아메리카',
        time: '경기 시작 : 오전 06~09시',
    },
];

const OWCS_CARDS: LeagueCard[] = [
    {
        leagueId: 5,
        code: 'ASIA',
        logo: owcsAsia,
        region: '지역 : 아시아',
        time: '경기 시작 : 오후 04~08시',
    },
    {
        leagueId: 6,
        code: 'CHINA',
        logo: owcsChina,
        region: '지역 : 중국',
        time: '경기 시작 : 오후 04~08시',
    },
    {
        leagueId: 7,
        code: 'NORTHAMERICA',
        logo: owcsNA,
        region: '지역 : 아메리카',
        time: '경기 시작 : 오전 06~09시',
    },
    {
        leagueId: 8,
        code: 'EMEA',
        logo: owcsEMEA,
        region: '지역 : 유럽,중동, 아프리카',
        time: '경기 시작 : 오전 12 ~ 03시',
    },
];

export default function PickAndAimLeagueSelect() {
    const router = useRouter();
    const params = useSearchParams();
    const mode = params.get('mode') === 'aim' ? 'aim' : 'normal';
    const game = (params.get('game') as GameCode) || 'VALORANT';
    const [league, setLeague] = useState<string | null>(null);

    const isValorant = game === 'VALORANT';
    const titleRight = useMemo(
        () => (mode === 'aim' ? '사격 : 리그 선택' : '일반 : 리그 선택'),
        [mode]
    );

    const subtitle = isValorant
        ? '발로란트 챔피언스 투어(VCT)'
        : '오버워치 챔피언스 시리즈(OWCS)';

    let leagueCards: LeagueCard[];
    if (isValorant) {
        leagueCards = VCT_CARDS;
    } else {
        leagueCards = OWCS_CARDS;
    }

    const handleNext = () => {
        if (!league) return;
        let id = 0;
        if (league !== 'NONE') {
            const found = leagueCards.find(c => c.code === league);
            if (found && typeof found.leagueId === 'number')
                id = found.leagueId;
        }
        const search = new URLSearchParams({
            mode,
            game,
            step: 'keywords',
            leagueId: String(id),
        });
        router.push(`/pick-and-aim/select?${search.toString()}`);
    };

    return (
        <div className="pickAimLeague">
            <header className="bar">
                <button className="back" onClick={() => router.back()}>
                    <Image
                        src={resourceBack}
                        alt="back"
                        width={64}
                        height={64}
                    />
                </button>
                <h1>선호하는 리그를 선택해주세요</h1>
                <span className="mode">{titleRight}</span>
                <div className="subtitle">{subtitle}</div>
            </header>

            <div className="leaguePanel">
                {/* 상단 '리그 없음' 선택 박스 */}
                <button
                    className={`noneSelect ${league === 'NONE' ? 'selected' : ''}`}
                    onClick={() => setLeague('NONE')}
                >
                    리그 없음
                </button>

                <div className="leagueGrid">
                    {leagueCards.map(card => (
                        <button
                            key={card.code}
                            className={`leagueCard ${league === card.code ? 'selected' : ''}`}
                            onClick={() => setLeague(card.code)}
                        >
                            <div className="logoWrap">
                                <Image
                                    src={card.logo}
                                    alt={card.code}
                                    fill
                                    sizes="25vw"
                                />
                            </div>
                            <ul className="desc">
                                <li>{card.region}</li>
                                <li>{card.time}</li>
                            </ul>
                        </button>
                    ))}
                </div>
            </div>

            <div className="footer">
                <TextButton
                    className={`start ${league ? 'primary' : 'secondary'}`}
                    text="다음"
                    func={handleNext}
                />
            </div>
        </div>
    );
}
