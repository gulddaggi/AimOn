'use client';
import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAppSelector, useAppDispatch } from '@/lib/redux/hooks';
import { resetLikesTeamList } from '@/lib/redux/feature/teams/likes-team-slice';
import Logo from '@/components/molecules/logo';
import RouteButton from '@/components/molecules/sidebar/route-button';
import ProfileButton from '@/components/molecules/sidebar/profile-button';
import HomeIconSvg from '@/resources/sidebar/resource-home-white.svg';
import LoginIconSvg from '@/resources/sidebar/resource-login-white.svg';
import CommunityIconSvg from '@/resources/sidebar/resource-community-white.svg';
import PaaIconSvg from '@/resources/sidebar/resource-paa-white.svg';
import { ImageElement } from '@/types/image';

const homeIcon: ImageElement = { src: HomeIconSvg.src, width: 40, height: 40 };
const communityIcon: ImageElement = {
    src: CommunityIconSvg.src,
    width: 40,
    height: 40,
};
const paaIcon: ImageElement = { src: PaaIconSvg.src, width: 40, height: 40 };
const loginIcon: ImageElement = {
    src: LoginIconSvg.src,
    width: 40,
    height: 40,
};

export default function SideBar() {
    const route = useRouter();
    const dispatch = useAppDispatch();
    const isLogined = useAppSelector(state => state.auth.isAuthorized);
    const isSuccess = useAppSelector(state => state.auth.isSucceeded);

    useEffect(() => {
        if (isSuccess === 'logout') {
            dispatch(resetLikesTeamList());
            route.push('/');
        }
    }, [isSuccess, route, dispatch]);

    return (
        <div className="sideBar">
            <Logo size="s" />
            <div className="routeButtons">
                <RouteButton
                    className="routeButton home"
                    page="/"
                    image={homeIcon}
                    alt="홈"
                />
                <RouteButton
                    className="routeButton community"
                    page="/community"
                    image={communityIcon}
                    alt="커뮤니티"
                />
                <RouteButton
                    className="routeButton pickaim"
                    page="/pick-and-aim"
                    image={paaIcon}
                    alt="Pick & Aim"
                />
            </div>
            <div className="userButtons">
                {isLogined ? (
                    <ProfileButton />
                ) : (
                    <RouteButton
                        className="routeButton login"
                        page="/login"
                        image={loginIcon}
                        alt="로그인 아이콘"
                    />
                )}
            </div>
        </div>
    );
}
