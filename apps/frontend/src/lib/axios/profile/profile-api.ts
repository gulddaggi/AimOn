'use server';

import { privateInstance } from '../axios-instance';
import { UserProfile } from '@/types/user';

export async function fetchMyProfile(): Promise<UserProfile | null> {
    try {
        const res = await privateInstance.get('/users/me');
        if (res.status === 200) {
            const data = res.data?.data ?? res.data;
            return {
                id: data.id,
                nickname: data.nickname,
                email: data.email ?? null,
                profileImageUrl: data.profileImageUrl ?? null,
                level: data.level ?? 0,
                exp: data.exp ?? 0,
            };
        }
        return null;
    } catch (e) {
        console.error('fetchMyProfile error', e);
        return null;
    }
}

export async function updateMyProfileImage(
    key: string
): Promise<UserProfile | null> {
    try {
        const res = await privateInstance.put('/users/me/profile-image', key, {
            headers: { 'Content-Type': 'application/json' },
        });
        const data = res.data?.data ?? res.data;
        return {
            id: data.id,
            nickname: data.nickname,
            email: data.email ?? null,
            profileImageUrl: data.profileImageUrl ?? null,
            level: data.level ?? 0,
            exp: data.exp ?? 0,
        } as UserProfile;
    } catch (e) {
        console.error('updateMyProfileImage error', e);
        return null;
    }
}

export async function updateMyProfile(params: {
    nickname: string;
    profileImageUrl: string | null;
}): Promise<UserProfile | null> {
    try {
        const res = await privateInstance.put('/users/me', params, {
            headers: { 'Content-Type': 'application/json' },
        });
        const data = res.data?.data ?? res.data;
        return {
            id: data.id,
            nickname: data.nickname,
            email: data.email ?? null,
            profileImageUrl: data.profileImageUrl ?? null,
            level: data.level ?? 0,
            exp: data.exp ?? 0,
        } as UserProfile;
    } catch (e) {
        console.error('updateMyProfile error', e);
        return null;
    }
}
