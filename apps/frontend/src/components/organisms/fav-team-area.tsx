'use client';
/* eslint-disable indent */
import { useRouter } from 'next/navigation';
import { useAppSelector } from '@/lib/redux/hooks';
import TextButton from '../atoms/buttons/text-button';
import { MatchWithTeam } from '@/types/match';
import Image from 'next/image';
import PlainTextBlock from '../atoms/text/plain-text-block';
import LoadingIcon from '../atoms/contents/loading-icon';

export default function FavTeamArea(props: {
    hasLikedTeam: boolean;
    prevMatch: MatchWithTeam[];
    nextMatch: MatchWithTeam[];
    isLoading?: boolean;
    selectedTeamId?: number;
    setSelectedTeamId?: (id: number) => void;
}) {
    const likedTeam = useAppSelector(state => state.likesTeam.likesTeams);
    const route = useRouter();

    function formatKST(date: Date): string {
        // 요구사항: 표기 시각을 원본에서 +23시간 이동하여 표시
        const t = new Date(date.getTime() + 32 * 60 * 60 * 1000);
        const y = t.getUTCFullYear();
        const m = String(t.getUTCMonth() + 1).padStart(2, '0');
        const d = String(t.getUTCDate()).padStart(2, '0');
        const hh = String(t.getUTCHours()).padStart(2, '0');
        const mm = String(t.getUTCMinutes()).padStart(2, '0');
        return `${y}-${m}-${d} ${hh}:${mm}`;
    }

    function getOpponentName(m: MatchWithTeam): string {
        const raw = (m as unknown as { opTeam?: string }).opTeam ?? '';
        if (raw && raw.trim().length > 0) return raw.trim();
        return 'TBD';
    }

    const selectedTeam = props.selectedTeamId
        ? likedTeam.find(team => team.id === props.selectedTeamId)
        : likedTeam[0];

    const currentSelectedId = props.selectedTeamId || likedTeam[0]?.id;
    return (
        <div className="fav-team-area">
            {props.hasLikedTeam ? (
                <div className="team-selection">
                    <div className="team-header">
                        <div className="team-info">
                            <Image
                                src={selectedTeam?.imgUrl || ''}
                                alt="선택된 팀"
                                width={40}
                                height={40}
                                className="team-logo"
                            />
                            <div className="team-details">
                                <PlainTextBlock
                                    className="team-name"
                                    text={selectedTeam?.teamName || ''}
                                />
                                <PlainTextBlock
                                    className="team-league"
                                    text="VCT Pacific"
                                />
                            </div>
                        </div>
                        <button
                            className="team-edit-button"
                            onClick={() => route.push('/pick-and-aim')}
                        >
                            <PlainTextBlock
                                className="edit-text"
                                text="선호 팀 수정/변경"
                            />
                            <span className="edit-icon">✏️</span>
                        </button>
                    </div>
                    <div className="team-selection-grid">
                        {Array.from({ length: 4 }).map((_, idx) => {
                            const team = likedTeam?.[idx];
                            if (team) {
                                const isSelected =
                                    currentSelectedId === team.id;
                                return (
                                    <div
                                        key={idx}
                                        className={`team-slot ${isSelected ? 'selected' : ''}`}
                                        onClick={() =>
                                            props.setSelectedTeamId?.(team.id)
                                        }
                                    >
                                        <div className="team-logo">
                                            <Image
                                                src={team.imgUrl}
                                                alt={team.teamName}
                                                width={60}
                                                height={60}
                                            />
                                        </div>
                                        <PlainTextBlock
                                            className="team-name"
                                            text={team.teamName}
                                        />
                                        <PlainTextBlock
                                            className="team-game"
                                            text="VALORANT"
                                        />
                                    </div>
                                );
                            } else {
                                return (
                                    <div key={idx} className="empty-team-slot">
                                        <div className="add-team-button">
                                            <PlainTextBlock
                                                className="add-team-text"
                                                text="+"
                                            />
                                        </div>
                                        <PlainTextBlock
                                            className="add-team-label"
                                            text="팀 추가"
                                        />
                                    </div>
                                );
                            }
                        })}
                    </div>
                </div>
            ) : (
                <div className="no-liked-team">
                    <TextButton
                        className="goToPickAndAim"
                        text="내 선호 팀 찾기"
                        func={() => {
                            route.push('/pick-and-aim');
                        }}
                    />
                </div>
            )}
            <section className="match-section">
                <header>
                    <PlainTextBlock
                        className="section-header"
                        text="최근 매치 결과"
                    />
                </header>
                <article className="match-list">
                    {props.isLoading ? (
                        <div className="loading-container">
                            <LoadingIcon />
                        </div>
                    ) : (
                        props.prevMatch.map((match, idx) => (
                            <div key={idx} className="match-item">
                                <PlainTextBlock
                                    className="match-date"
                                    text={formatKST(match.matchDate)}
                                />
                                <div className="match-teams">
                                    {match.teamIcon && (
                                        <Image
                                            src={match.teamIcon}
                                            alt={`${match.teamName} 아이콘`}
                                            width={24}
                                            height={24}
                                            className="team-icon"
                                        />
                                    )}
                                    <PlainTextBlock
                                        className="team-name"
                                        text={`${match.teamName} vs ${getOpponentName(match)}`}
                                    />
                                    {getOpponentName(match) !== 'TBD' &&
                                        match.opTeamIcon && (
                                            <Image
                                                src={match.opTeamIcon}
                                                alt={`${getOpponentName(match)} 아이콘`}
                                                width={24}
                                                height={24}
                                                className="team-icon"
                                            />
                                        )}
                                </div>
                                <PlainTextBlock
                                    className="match-score"
                                    text={`${match.myScore} - ${match.opScore}`}
                                />
                            </div>
                        ))
                    )}
                </article>
            </section>
            <section className="match-section">
                <header>
                    <PlainTextBlock
                        className="section-header"
                        text="다음 경기 일정"
                    />
                </header>
                <article className="match-list">
                    {props.isLoading ? (
                        <div className="loading-container">
                            <LoadingIcon />
                        </div>
                    ) : (
                        props.nextMatch.map((match, idx) => (
                            <div key={idx} className="match-item">
                                <PlainTextBlock
                                    className="match-date"
                                    text={formatKST(match.matchDate)}
                                />
                                <div className="match-teams">
                                    {match.teamIcon && (
                                        <Image
                                            src={match.teamIcon}
                                            alt={`${match.teamName} 아이콘`}
                                            width={24}
                                            height={24}
                                            className="team-icon"
                                        />
                                    )}
                                    <PlainTextBlock
                                        className="team-name"
                                        text={`${match.teamName} vs ${getOpponentName(match)}`}
                                    />
                                    {getOpponentName(match) !== 'TBD' &&
                                        match.opTeamIcon && (
                                            <Image
                                                src={match.opTeamIcon}
                                                alt={`${getOpponentName(match)} 아이콘`}
                                                width={24}
                                                height={24}
                                                className="team-icon"
                                            />
                                        )}
                                </div>
                            </div>
                        ))
                    )}
                </article>
            </section>
        </div>
    );
}
