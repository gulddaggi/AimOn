'use client';
import { useState } from 'react';
import Image from 'next/image';
import PlainTextBlock from '../atoms/text/plain-text-block';
import { Player } from '@/types/player';

interface PlayerCardProps {
    players: Player[];
}

export default function PlayerCard({ players }: PlayerCardProps) {
    const [currentIndex, setCurrentIndex] = useState(0);

    if (!players || players.length === 0) {
        return <div className="player-card empty">선수 정보가 없습니다.</div>;
    }

    const player = players[currentIndex];
    const { handle, name, country, teamName, imgUrl, valorantStats } = player;
    const { round, acs, kda, cl } = valorantStats;

    // 클러치를 백분율로 변환
    const clutchPercentage = Math.round(cl * 100);

    const goToPrevious = () => {
        setCurrentIndex(prev => (prev === 0 ? players.length - 1 : prev - 1));
    };

    const goToNext = () => {
        setCurrentIndex(prev => (prev === players.length - 1 ? 0 : prev + 1));
    };

    return (
        <div className="player-card">
            <div className="player-image">
                <Image
                    src={imgUrl || '/default-player.png'}
                    alt={`${handle} 선수`}
                    width={125}
                    height={0}
                    className="player-photo"
                    style={{ height: 'auto' }}
                />
            </div>

            <div className="player-info">
                <div className="player-basic">
                    <div className="player-name">
                        <PlainTextBlock className="handle" text={handle} />
                        <PlainTextBlock className="real-name" text={name} />
                    </div>

                    <div className="player-details">
                        <div className="country">
                            <PlainTextBlock className="label" text="국가" />
                            <PlainTextBlock className="value" text={country} />
                        </div>

                        <div className="team">
                            <PlainTextBlock className="label" text="소속:" />
                            <PlainTextBlock className="value" text={teamName} />
                        </div>
                    </div>
                </div>

                <div className="player-stats">
                    <div className="stat-item">
                        <PlainTextBlock
                            className="stat-text"
                            text={`플레이한 라운드 : ${round}`}
                        />
                    </div>

                    <div className="stat-item">
                        <PlainTextBlock
                            className="stat-text"
                            text={`평균 전투 점수(ACS) : ${acs}`}
                        />
                    </div>

                    <div className="stat-row">
                        <div className="stat-item">
                            <PlainTextBlock
                                className="stat-text"
                                text={`K/DA : ${kda}`}
                            />
                        </div>

                        <div className="stat-item">
                            <PlainTextBlock
                                className="stat-text"
                                text={`클러치 : ${clutchPercentage}%`}
                            />
                        </div>
                    </div>
                </div>
            </div>

            {/* 네비게이션 */}
            {players.length > 1 && (
                <div className="player-navigation">
                    <button className="nav-button prev" onClick={goToPrevious}>
                        &#8249;
                    </button>

                    {players.length <= 7 && (
                        <div className="page-indicators">
                            {players.map((_, index) => (
                                <button
                                    key={index}
                                    className={`indicator ${index === currentIndex ? 'active' : ''}`}
                                    onClick={() => setCurrentIndex(index)}
                                />
                            ))}
                        </div>
                    )}

                    <button className="nav-button next" onClick={goToNext}>
                        &#8250;
                    </button>
                </div>
            )}
        </div>
    );
}
