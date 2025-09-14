'use client';
import { useMemo, useState } from 'react';
import Image from 'next/image';
import TextButton from '@/components/atoms/buttons/text-button';
import { useRouter, useSearchParams } from 'next/navigation';
import resourceBack from '@/resources/pick-and-aim/resource-paa-back.svg';
import thumbValorant from '@/resources/pick-and-aim/thumbnail-valorant.jpg';
import thumbOverwatch from '@/resources/pick-and-aim/thumbnail-overwatch2.png';
import logoValorant from '@/resources/pick-and-aim/logo-valorant.png';
import logoOverwatch from '@/resources/pick-and-aim/logo-overwatch2.png';
import gifValorant from '@/resources/pick-and-aim/gif-valorant.gif';
import gifOverwatch from '@/resources/pick-and-aim/git-overwatch2.gif';

type GameCode = 'VALORANT' | 'OVERWATCH2';

export default function PickAndAimGameSelect() {
    const router = useRouter();
    const params = useSearchParams();
    const mode = params.get('mode') === 'aim' ? 'aim' : 'normal';
    const [game, setGame] = useState<GameCode | null>(null);

    const titleRight = useMemo(
        () => (mode === 'aim' ? '사격 : 게임 선택' : '일반 : 게임 선택'),
        [mode]
    );

    const handleNext = () => {
        if (!game) return;
        const search = new URLSearchParams({ mode, game, step: 'league' });
        router.push(`/pick-and-aim/select?${search.toString()}`);
    };

    return (
        <div className="pickAimGame">
            <header className="bar">
                <button className="back" onClick={() => router.back()}>
                    <Image
                        src={resourceBack}
                        alt="back"
                        width={64}
                        height={64}
                    />
                </button>
                <h1>선호하는 게임을 선택해주세요</h1>
                <span className="mode">{titleRight}</span>
            </header>

            <div className="gameGrid">
                <button
                    className={`gameCard valorant ${game === 'VALORANT' ? 'selected' : ''}`}
                    onClick={() => setGame('VALORANT')}
                >
                    {game === 'VALORANT' ? (
                        <div className="detail">
                            <div className="gif">
                                <Image
                                    src={gifValorant}
                                    alt="valorant gameplay"
                                    fill
                                    sizes="50vw"
                                    style={{
                                        objectFit: 'contain',
                                        objectPosition: 'center',
                                    }}
                                />
                            </div>
                            <ul className="desc">
                                <li>정확한 에임 중심</li>
                                <li>전략적인 스킬 조합</li>
                                <li>침착한 심리전 플레이</li>
                            </ul>
                        </div>
                    ) : (
                        <div className="image">
                            <Image
                                src={thumbValorant}
                                alt="valorant"
                                fill
                                sizes="50vw"
                                style={{ objectFit: 'cover' }}
                            />
                        </div>
                    )}
                    <div className="label">
                        <Image
                            src={logoValorant}
                            alt="VALORANT"
                            width={300}
                            height={150}
                        />
                    </div>
                </button>
                <button
                    className={`gameCard overwatch ${game === 'OVERWATCH2' ? 'selected' : ''}`}
                    onClick={() => setGame('OVERWATCH2')}
                >
                    {game === 'OVERWATCH2' ? (
                        <div className="detail">
                            <div className="gif">
                                <Image
                                    src={gifOverwatch}
                                    alt="overwatch2 gameplay"
                                    fill
                                    sizes="50vw"
                                    style={{
                                        objectFit: 'contain',
                                        objectPosition: 'center',
                                    }}
                                />
                            </div>
                            <ul className="desc">
                                <li>빠른 팀파이트 중심</li>
                                <li>궁극기 연계 플레이</li>
                                <li>동시 교전의 몰입감</li>
                            </ul>
                        </div>
                    ) : (
                        <div className="image">
                            <Image
                                src={thumbOverwatch}
                                alt="overwatch2"
                                fill
                                sizes="50vw"
                                style={{ objectFit: 'cover' }}
                            />
                        </div>
                    )}
                    <div className="label">
                        <Image
                            src={logoOverwatch}
                            alt="OVERWATCH"
                            width={350}
                            height={250}
                        />
                    </div>
                </button>
            </div>

            <div className="footer">
                <TextButton
                    className={`start ${game ? 'primary' : 'secondary'}`}
                    text="다음"
                    func={handleNext}
                />
            </div>
        </div>
    );
}
