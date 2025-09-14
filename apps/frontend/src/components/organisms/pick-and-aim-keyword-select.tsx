'use client';
import Image from 'next/image';
import TextButton from '@/components/atoms/buttons/text-button';
import LoadingIcon from '@/components/atoms/contents/loading-icon';
import { useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import resourceBack from '@/resources/pick-and-aim/resource-paa-back.svg';

type Keyword = { key: string; displayName: string; description: string };

const MAX_SELECTION = 3;

export default function PickAndAimKeywordSelect() {
    const router = useRouter();
    const params = useSearchParams();
    const mode = params.get('mode') === 'aim' ? 'aim' : 'normal';
    const game = params.get('game') || 'VALORANT';
    const leagueId = params.get('leagueId') || '0';
    const [selected, setSelected] = useState<string[]>([]);
    const [keywords, setKeywords] = useState<Keyword[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    const titleRight = useMemo(
        () => (mode === 'aim' ? '사격 : 키워드 선택' : '일반 : 키워드 선택'),
        [mode]
    );

    const toggleKeyword = (kw: string) => {
        setSelected(prev => {
            const has = prev.includes(kw);
            if (has) return prev.filter(k => k !== kw);
            if (prev.length >= MAX_SELECTION) return prev; // 최대 3개 제한
            return [...prev, kw];
        });
    };

    useEffect(() => {
        let aborted = false;
        (async () => {
            try {
                const res = await fetch('/next-api/pick-aim/keywords', {
                    cache: 'no-store',
                    credentials: 'include',
                });
                if (!res.ok) return;
                const data: Keyword[] = await res.json();
                if (!aborted && Array.isArray(data)) setKeywords(data);
            } catch {
            } finally {
                if (!aborted) setLoading(false);
            }
        })();
        return () => {
            aborted = true;
        };
    }, []);

    const handleNext = () => {
        if (selected.length !== MAX_SELECTION) return;
        const names = selected
            .map(k => keywords.find(x => x.key === k)?.displayName || k)
            .join(',');
        const search = new URLSearchParams({
            mode,
            game,
            leagueId,
            step: 'teams',
            keywords: selected.join(','),
            keywordNames: names,
        });
        router.push(`/pick-and-aim/select?${search.toString()}`);
    };

    return (
        <div className="pickAimKeyword">
            <header className="bar">
                <button className="back" onClick={() => router.back()}>
                    <Image
                        src={resourceBack}
                        alt="back"
                        width={64}
                        height={64}
                    />
                </button>
                <h1>선호하는 키워드를 선택해주세요!</h1>
                <span className="mode">{titleRight}</span>
            </header>

            <div className="panel">
                <div className="panelHeader">
                    <span className="selectedCount">
                        선택 키워드 {selected.length} / {MAX_SELECTION}
                    </span>
                </div>
                <div className="keywords">
                    {loading ? (
                        <div className="loadingWrap">
                            <LoadingIcon />
                            <div className="loadingText">
                                키워드를 불러오는 중입니다.
                            </div>
                        </div>
                    ) : keywords.length === 0 ? (
                        <div className="no-keywords">키워드 없음</div>
                    ) : (
                        keywords.map(k => (
                            <div key={k.key} className="keywordWrap">
                                <button
                                    className={`keyword ${selected.includes(k.key) ? 'selected' : ''}`}
                                    onClick={() => toggleKeyword(k.key)}
                                >
                                    {k.displayName}
                                </button>
                                <div className="tooltip">{k.description}</div>
                            </div>
                        ))
                    )}
                </div>
            </div>

            <div className="footer">
                <TextButton
                    className={`start ${selected.length === MAX_SELECTION ? 'primary' : 'secondary'}`}
                    text="다음"
                    func={handleNext}
                />
            </div>
        </div>
    );
}
