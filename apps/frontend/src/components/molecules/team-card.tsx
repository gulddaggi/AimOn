'use client';
import { useState } from 'react';
import Image from 'next/image';
import PlainTextBlock from '../atoms/text/plain-text-block';
import { Team } from '@/types/team';

interface TeamCardProps {
    teams: Team[];
}

export default function TeamCard({ teams }: TeamCardProps) {
    const [currentIndex, setCurrentIndex] = useState(0);

    if (!teams || teams.length === 0) {
        return <div className="team-card empty">팀 정보가 없습니다.</div>;
    }

    const team = teams[currentIndex];
    const { teamName, country, winRate, point, imgUrl } = team;

    const goToPrevious = () => {
        setCurrentIndex(prev => (prev === 0 ? teams.length - 1 : prev - 1));
    };

    const goToNext = () => {
        setCurrentIndex(prev => (prev === teams.length - 1 ? 0 : prev + 1));
    };

    return (
        <div className="team-card">
            <div className="team-header">
                <PlainTextBlock className="team-info-header" text="팀 정보" />
            </div>

            <div className="team-main">
                <div className="team-logo">
                    <Image
                        src={imgUrl || '/default-team.png'}
                        alt={`${teamName} 로고`}
                        width={120}
                        height={120}
                        className="logo-image"
                    />
                </div>

                <div className="team-info">
                    <div className="team-basic">
                        <PlainTextBlock className="team-name" text={teamName} />
                        <PlainTextBlock
                            className="team-country"
                            text={country}
                        />
                    </div>

                    <div className="team-stats">
                        <div className="stat-item">
                            <PlainTextBlock
                                className="stat-text"
                                text={`승률: ${winRate}%`}
                            />
                        </div>

                        <div className="stat-item">
                            <PlainTextBlock
                                className="stat-text"
                                text={`챔피언십 포인트: ${point} 포인트`}
                            />
                        </div>
                    </div>
                </div>
            </div>

            {/* 네비게이션 */}
            {teams.length > 1 && (
                <div className="team-navigation">
                    <button className="nav-button prev" onClick={goToPrevious}>
                        &#8249;
                    </button>

                    <div className="page-indicators">
                        {teams.map((_, index) => (
                            <button
                                key={index}
                                className={`indicator ${index === currentIndex ? 'active' : ''}`}
                                onClick={() => setCurrentIndex(index)}
                            />
                        ))}
                    </div>

                    <button className="nav-button next" onClick={goToNext}>
                        &#8250;
                    </button>
                </div>
            )}
        </div>
    );
}
