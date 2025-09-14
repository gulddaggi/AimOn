import PlainTextBlock from '@/components/atoms/text/plain-text-block';
import LoadingIcon from '@/components/atoms/contents/loading-icon';
import GuideBlock from '@/components/molecules/chatbot/guide-block';
import { Message } from '@/types/message';
import { useEffect, useRef } from 'react';

export default function MessagesArea(props: {
    data: Message[];
    isLoading: boolean;
}) {
    // 자동 스크롤 비활성화 (입문가이드 버튼 클릭 시 하단으로 이동하지 않도록)
    const containerRef = useRef<HTMLDivElement | null>(null);

    // 패널(messageArea) 컨테이너에만 자동 스크롤 적용
    // 데이터 또는 로딩 상태 변경 후 레이아웃이 그려진 다음에 스크롤 이동
    useEffect(() => {
        const el = containerRef.current;
        if (!el) return;
        requestAnimationFrame(() => {
            el.scrollTop = el.scrollHeight;
        });
    }, [props.data, props.isLoading]);

    return (
        <div className="messageArea" ref={containerRef}>
            {props.data.map((item, idx) => {
                if (item.sender === 'guide') {
                    return (
                        <div key={idx}>
                            <GuideBlock data={item.context} />
                        </div>
                    );
                }

                return (
                    <div key={idx}>
                        <PlainTextBlock
                            className={item.sender.concat('Msg')}
                            text={item.context as string}
                        />
                    </div>
                );
            })}
            {props.isLoading ? <LoadingIcon /> : null}
        </div>
    );
}
