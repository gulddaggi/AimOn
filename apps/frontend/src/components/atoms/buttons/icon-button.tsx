// 이미지 아이콘 버튼
// 함수 기반 상호작용 컴포넌트
'use client';

import Image from 'next/image';
// 이미지 객체 타입 정의, svg 파일은 기본적으로 호환됨
import { ImageElement } from '@/types/image';

// 클래스명, 이미지, 대체어, 함수 필요
export default function IconButton(props: {
    className: string;
    image: ImageElement;
    alt: string;
    func?: (e?: React.MouseEvent) => void;
}) {
    return (
        <>
            <button
                className={`iconButton ${props.className}`}
                onClick={e => props.func?.(e)}
            >
                <Image
                    src={props.image.src}
                    width={props.image.width}
                    height={props.image.height}
                    alt={props.alt}
                />
            </button>
        </>
    );
}
