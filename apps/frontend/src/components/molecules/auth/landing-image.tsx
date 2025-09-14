import loginImage from '@/resources/login/login-image.svg';
import LocalImage from '@/components/atoms/contents/local-image';

export default function LandingImage() {
    return (
        <>
            <LocalImage
                className="landingImage"
                image={loginImage}
                alt="랜딩 이미지"
                width={350}
                height={350}
            />
        </>
    );
}
