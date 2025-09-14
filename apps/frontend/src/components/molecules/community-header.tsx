'use client';

import { useRouter } from 'next/navigation';
import backIcon from '@/resources/resource-back-white.svg';
import IconButton from '@/components/atoms/buttons/icon-button';
import { ImageElement } from '@/types/image';

type Props = {
    title: string;
    showBack?: boolean;
    className?: string;
};

export default function CommunityHeader({
    title,
    showBack = false,
    className,
}: Props) {
    const router = useRouter();
    const backImg: ImageElement = {
        src: backIcon.src,
        width: 24,
        height: 24,
    };

    return (
        <div className={`communityHeaderPanel writeHeader ${className ?? ''}`}>
            {showBack ? (
                <IconButton
                    className="headerBackButton"
                    image={backImg}
                    alt="back"
                    func={() => router.back()}
                />
            ) : (
                <div className="headerBackSpace" />
            )}
            <h1 className="communityPageTitle">{title}</h1>
            <div className="headerBackSpace" />
        </div>
    );
}
