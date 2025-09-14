// Text with Icon = twi
'use client';
import { ImageElement } from '@/types/image';
import Image from 'next/image';

export default function TwiButton(props: {
    className: string;
    text: string;
    image: ImageElement;
    alt: string;
    func: () => void;
}) {
    return (
        <>
            <button
                className={`twiButton ${props.className}`}
                onClick={props.func}
            >
                <span>{props.text}</span>
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
