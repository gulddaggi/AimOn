'use client';
import { useState } from 'react';
import IconButton from '@/components/atoms/buttons/icon-button';
import ProfileIcon from '@/resources/sidebar/resource-profile-white.svg';
import ProfileSub from './profile-sub';
import { ImageElement } from '@/types/image';

export default function ProfileButton() {
    const [isMoused, setIsMoused] = useState(false);
    const [iconSize, setIconSize] = useState({ width: 40, height: 40 });

    function setMouseFlagOn() {
        setIconSize({ width: 45, height: 45 });
        setIsMoused(true);
    }

    function setMouseFlagOff() {
        setIconSize({ width: 40, height: 40 });
        setIsMoused(false);
    }

    const resizedProfileIcon: ImageElement = {
        src: ProfileIcon.src,
        ...iconSize,
    };

    return (
        <div
            className="profileButton"
            onMouseEnter={setMouseFlagOn}
            onMouseLeave={setMouseFlagOff}
        >
            <IconButton
                className="profile"
                image={resizedProfileIcon}
                alt="프로필 아이콘"
                func={setMouseFlagOn}
            />
            {isMoused ? (
                <ProfileSub on={setMouseFlagOn} off={setMouseFlagOff} />
            ) : null}
        </div>
    );
}
