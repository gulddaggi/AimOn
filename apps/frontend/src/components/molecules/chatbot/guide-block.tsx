'use client';
import Image from 'next/image';
import { useAppDispatch } from '@/lib/redux/hooks';
import { fetchGuide, addMessage } from '@/lib/redux/feature/chatbot/chat-slice';
import TextButton from '@/components/atoms/buttons/text-button';
import PlainTextBlock from '@/components/atoms/text/plain-text-block';
import PlayercardSimple from '@/components/molecules/playercard-simple';
import { GuideResponse } from '@/types/guide';

export default function GuideBlock(props: { data: string | GuideResponse }) {
    const dispatch = useAppDispatch();

    // 에러 메시지인 경우 (string)
    if (typeof props.data === 'string') {
        return <PlainTextBlock text={props.data} className="guideMsg" />;
    }

    // 정상 데이터인 경우 (GuideResponse)
    const guideData = props.data;

    return (
        <div className="guideBlock">
            {/* 팀 아이콘이 있으면 개요 위에 출력 */}
            {guideData.teamIcon && (
                <div className="teamIconContainer">
                    <Image
                        src={guideData.teamIcon}
                        alt="Team Icon"
                        width={100}
                        height={100}
                    />
                </div>
            )}

            {/* 개요가 있으면 PlainTextBlock으로 출력 */}
            {guideData.outline && (
                <PlainTextBlock text={guideData.outline} className="guideMsg" />
            )}

            {/* keys가 있으면 각각 TextButton으로 출력 */}
            {guideData.keys && guideData.keys.length > 0 && (
                <div className="guideButtons">
                    {guideData.keys.map((key, index) => {
                        const totalKeys = guideData.keys!.length;
                        const lastRowStart =
                            Math.floor((totalKeys - 1) / 7) * 7;
                        const isLastRow = index >= lastRowStart;

                        return (
                            <TextButton
                                key={index}
                                className={`guideButton ${isLastRow ? 'lastRow' : ''}`}
                                text={key}
                                func={() => {
                                    dispatch(
                                        addMessage({
                                            context: key,
                                            sender: 'user',
                                        })
                                    );
                                    dispatch(fetchGuide(key));
                                }}
                            />
                        );
                    })}
                </div>
            )}

            {/* players가 있으면 PlayercardSimple로 출력 */}
            {guideData.players && guideData.players.length > 0 && (
                <div className="playersContainer">
                    {guideData.players.map((player, index) => (
                        <PlayercardSimple key={index} player={player} />
                    ))}
                </div>
            )}
        </div>
    );
}
