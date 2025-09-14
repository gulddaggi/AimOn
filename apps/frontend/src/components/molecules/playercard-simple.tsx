'use client';
import Image from 'next/image';
import PlainTextBlock from '@/components/atoms/text/plain-text-block';
import { Player } from '@/types/guide';

export default function PlayercardSimple(props: { player: Player }) {
    const { player } = props;
    
    return (
        <div className="playercardSimple">
            <div className="playerAvatar">
                <Image
                    src={player.url}
                    alt={player.handle}
                    width={60}
                    height={60}
                />
            </div>
            <div className="playerInfo">
                <div className="playerNameRow">
                    <PlainTextBlock text={player.handle} className="playerHandle" />
                    <PlainTextBlock text={player.country} className="playerCountry" />
                </div>
                <PlainTextBlock text={player.name} className="playerName" />
            </div>
        </div>
    );
}