import logoImg from '@/resources/logo.svg';
import LocalImage from '../atoms/contents/local-image';

export default function Logo(props: { size: string; name?: string }) {
    let width: number = 1000;
    let height: number = 1000;

    switch (props.size) {
        case 'xs':
            width = 64;
            height = 26;
            break;
        case 's':
            width = 96;
            height = 39;
            break;
        case 'm':
            width = 128;
            height = 52;
            break;
        case 'l':
            width = 192;
            height = 72;
            break;
        case 'extra':
            width = 264;
            height = 104;
            break;
    }

    return (
        <>
            <LocalImage
                className={`logo ${props.size} ${props.name}`}
                image={logoImg}
                alt="로고 이미지"
                width={width}
                height={height}
            />
        </>
    );
}
