'use client';
import { useRouter } from 'next/navigation';
import { useAppDispatch } from '@/lib/redux/hooks';
import { logout } from '@/lib/redux/feature/auth/auth-slice';
import TextButton from '@/components/atoms/buttons/text-button';

export default function ProfileSub(props: { on: () => void; off: () => void }) {
    const route = useRouter();
    const dispatch = useAppDispatch();

    async function logoutEvent() {
        dispatch(logout());
        props.off();
    }

    return (
        <div
            className="profileSub"
            onMouseEnter={props.on}
            onMouseLeave={props.off}
        >
            <TextButton
                className="profileSubBtn myPage"
                text="마이페이지"
                func={() => {
                    props.off();
                    route.push('/my-page');
                }}
            />
            <TextButton
                className="profileSubBtn logout"
                text="로그아웃"
                func={logoutEvent}
            />
        </div>
    );
}
