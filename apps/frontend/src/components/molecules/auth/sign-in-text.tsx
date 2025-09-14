import PlainTextInline from '@/components/atoms/text/plain-text-inline';
import HyperlinkRoute from '@/components/atoms/text/hyperlink-route';

export default function SignInText() {
    return (
        <div className="signInText">
            <PlainTextInline
                className="signInText"
                text="계정이 존재하지 않는다면? "
            />
            <HyperlinkRoute
                className="signInText"
                text="회원가입"
                href="/signIn"
            />
        </div>
    );
}
