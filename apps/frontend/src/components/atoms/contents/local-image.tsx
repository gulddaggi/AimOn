import Image from 'next/image';
import { ImageElement } from '@/types/image';

export default function LocalImage(props: {
    className: string;
    image: ImageElement;
    alt: string;
    width?: number;
    height?: number;
}) {
    const width = props.width === undefined ? props.image.width : props.width;
    const height =
        props.height === undefined ? props.image.height : props.height;

    return (
        <Image
            className={props.className}
            src={props.image.src}
            alt={props.alt}
            width={width}
            height={height}
        ></Image>
    );
}
