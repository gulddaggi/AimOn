'use client';
import { useRouter } from 'next/navigation';
import IconButton from '@/components/atoms/buttons/icon-button';
import { ImageElement } from '@/types/image';

export default function RouteButton(props: {
    className: string;
    page: string;
    image: ImageElement;
    alt: string;
}) {
    const router = useRouter();

    return (
        <>
            <IconButton
                className={props.className}
                image={props.image}
                alt={props.alt}
                func={() => router.push(props.page)}
            />
        </>
    );
}
