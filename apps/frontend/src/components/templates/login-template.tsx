import LandingImage from '../molecules/auth/landing-image';
import SignInText from '../molecules/auth/sign-in-text';
import Logo from '../molecules/logo';
import LoginForm from '../organisms/login-form';
import LoginToast from '../molecules/auth/login-toast';

export default function LoginTemplate() {
    return (
        <>
            <LoginToast />
            <div className="authTemplate login">
                <div className="actionSection">
                    <div className="landingCol">
                        <LandingImage />
                    </div>
                    <div className="formCol">
                        <Logo size="extra" />
                        <LoginForm />
                        <SignInText />
                    </div>
                </div>
            </div>
        </>
    );
}
