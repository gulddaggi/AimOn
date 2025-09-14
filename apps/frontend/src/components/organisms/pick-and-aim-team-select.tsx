'use client';
import Image from 'next/image';
import TextButton from '@/components/atoms/buttons/text-button';
import LoadingIcon from '@/components/atoms/contents/loading-icon';
import { useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import resourceBack from '@/resources/pick-and-aim/resource-paa-back.svg';

type Candidate = {
    teamId: number;
    teamName: string;
    leagueId: number;
    leagueName: string;
    totalScore: number;
    rank?: number;
};

type TeamDetail = {
    id: number;
    gameId: number;
    leagueId: number;
    teamName: string;
    country: string;
    winRate: number;
    attackWinRate: number;
    defenseWinRate: number;
    imgUrl: string;
    point: number;
    rank?: number;
};

const MAX_SELECTION = 2;

export default function PickAndAimTeamSelect() {
    const router = useRouter();
    const params = useSearchParams();
    const mode = params.get('mode') === 'aim' ? 'aim' : 'normal';
    const leagueId = Number(params.get('leagueId') || '0');
    // const game = params.get('game') || 'VALORANT';
    const keywordsParam = params.get('keywords') || '';
    const keywordNamesParam = params.get('keywordNames') || '';
    const [selected, setSelected] = useState<number[]>([]);
    const [loading, setLoading] = useState(true);
    const [candidates, setCandidates] = useState<Candidate[]>([]);
    const [details, setDetails] = useState<Record<number, TeamDetail>>({});

    const titleRight = useMemo(
        () => (mode === 'aim' ? '사격 : 선호 팀 선택' : '일반 : 선호 팀 선택'),
        [mode]
    );

    // gameId 매핑: 현재 1=VALORANT, 2=OVERWATCH2 (요구사항: gameId=1 사용)
    const gameId = 1;

    useEffect(() => {
        let aborted = false;
        (async () => {
            try {
                setLoading(true);
                const keywords = keywordsParam
                    .split(',')
                    .map(k => k.trim())
                    .filter(Boolean);
                const res = await fetch('/next-api/pick-aim/candidates', {
                    method: 'POST',
                    credentials: 'include',
                    cache: 'no-store',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ gameId, leagueId, keywords }),
                });
                if (!res.ok) throw new Error('Failed to fetch candidates');
                const data: Candidate[] = await res.json();
                if (aborted) return;
                setCandidates(data);
                // 팀 상세 병렬 조회
                const ids = data.map(c => c.teamId);
                const results = await Promise.all(
                    ids.map(id =>
                        fetch(`/next-api/teams/${id}`, {
                            credentials: 'include',
                            cache: 'no-store',
                        }).then(r => (r.ok ? r.json() : null))
                    )
                );
                if (aborted) return;
                const map: Record<number, TeamDetail> = {};
                results.forEach(td => {
                    if (td && typeof td.id === 'number')
                        map[td.id] = td as TeamDetail;
                });
                setDetails(map);
            } catch {
            } finally {
                if (!aborted) setLoading(false);
            }
        })();
        return () => {
            aborted = true;
        };
    }, [keywordsParam, leagueId]);

    // 표시 용도는 key 그대로 사용

    const toggleTeam = (id: number) => {
        setSelected(prev => {
            const has = prev.includes(id);
            if (has) return prev.filter(t => t !== id);
            if (prev.length >= MAX_SELECTION) return prev;
            return [...prev, id];
        });
    };

    const handleDone = () => {
        if (selected.length === 0) return;
        (async () => {
            try {
                // 1) 현재 지정된 선호 팀 해제
                const myRes = await fetch('/next-api/likes/teams/me', {
                    credentials: 'include',
                    cache: 'no-store',
                });
                if (myRes.ok) {
                    const liked: Array<{ id: number }> = await myRes.json();
                    for (const t of liked) {
                        const res = await fetch(
                            `/next-api/likes/teams/${t.id}`,
                            {
                                method: 'POST',
                                credentials: 'include',
                                cache: 'no-store',
                            }
                        );
                        if (!res.ok) throw new Error('toggle failed');
                    }
                }
                // 2) 선택한 팀을 순서대로 추가 토글
                for (const id of selected) {
                    const res = await fetch(`/next-api/likes/teams/${id}`, {
                        method: 'POST',
                        credentials: 'include',
                        cache: 'no-store',
                    });
                    if (!res.ok) throw new Error('toggle failed');
                }
                // 완료 후 홈으로 이동 + 토스트 메시지 쿼리 전달
                const sp = new URLSearchParams({
                    toast: '선호 팀이 저장되었습니다.',
                });
                router.push(`/?${sp.toString()}`);
            } catch {
                const sp = new URLSearchParams({
                    toast: '선호 팀 저장 중 오류가 발생했습니다.',
                });
                router.push(`/?${sp.toString()}`);
            }
        })();
    };

    return (
        <div className="pickAimTeam">
            <header className="bar">
                <button className="back" onClick={() => router.back()}>
                    <Image
                        src={resourceBack}
                        alt="back"
                        width={64}
                        height={64}
                    />
                </button>
                <h1>Pick & Aim 결과</h1>
                <span className="mode">{titleRight}</span>
                <div className="subtitle">선호 팀을 최대 2개 선택해주세요!</div>
            </header>

            <div className="panel">
                <div className="panelHeader">
                    <span className="selectedCount">
                        선택 선호 팀 {selected.length} / {MAX_SELECTION}
                    </span>
                    <div className="selectedKeywords">
                        선택 키워드:
                        <span className="list">
                            {keywordNamesParam
                                .split(',')
                                .map(k => k.trim())
                                .filter(Boolean)
                                .join(', ')}
                        </span>
                    </div>
                </div>
                <div className="teamGrid">
                    {!loading &&
                        candidates.map((c: Candidate) => {
                            const detail = details[c.teamId];
                            const imgUrl = detail?.imgUrl || '';
                            return (
                                <button
                                    key={c.teamId}
                                    className={`teamCard ${selected.includes(c.teamId) ? 'selected' : ''}`}
                                    onClick={() => toggleTeam(c.teamId)}
                                >
                                    <div className="logoWrap">
                                        {imgUrl ? (
                                            <Image
                                                src={imgUrl}
                                                alt={c.teamName}
                                                fill
                                                sizes="25vw"
                                            />
                                        ) : (
                                            <div className="noLogo" />
                                        )}
                                    </div>
                                    <div className="name">{c.teamName}</div>
                                    <ul className="desc">
                                        <li>소속 리그 : {c.leagueName}</li>
                                        <li>리그 내 순위 : {c.rank ?? 0}</li>
                                    </ul>
                                </button>
                            );
                        })}
                </div>
                {loading && (
                    <div className="loadingOverlay">
                        <div className="loadingWrap">
                            <LoadingIcon />
                            <div className="loadingText">
                                후보 팀을 불러오는 중입니다.
                            </div>
                        </div>
                    </div>
                )}
            </div>

            <div className="footer">
                <TextButton
                    className={`start ${selected.length >= 1 ? 'primary' : 'secondary'}`}
                    text="선택 완료"
                    func={handleDone}
                />
            </div>
        </div>
    );
}
