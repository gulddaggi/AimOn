import SignInForm from '../organisms/sign-in-form';
import LandingImage from '../molecules/auth/landing-image';
import Logo from '../molecules/logo';
import SignInToast from '../molecules/auth/sign-in-toast';

export default function SignInTemplate() {
    return (
        <>
            <SignInToast />
            <div className="authTemplate signIn">
                <div className="actionSection">
                    <div className="landingCol">
                        <LandingImage />
                    </div>
                    <div className="formCol">
                        <Logo size="extra" />
                        <SignInForm />
                    </div>
                </div>
            </div>
        </>
    );
}
