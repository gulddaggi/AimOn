'use server';

import { privateInstance } from '@/lib/axios/axios-instance';

type MyPostRow = {
    postId: number;
    authorId?: number;
    authorNickname?: string;
    createdAt?: string;
    title?: string;
    body?: string;
    likeCount?: number;
    commentCount?: number;
};

export async function fetchMyPostsRaw(): Promise<MyPostRow[]> {
    try {
        const res = await privateInstance.get('/posts/my');
        const data = (res.data?.data ?? res.data) as unknown;
        return Array.isArray(data) ? (data as MyPostRow[]) : [];
    } catch {
        return [];
    }
}

export async function fetchLikedPostsRaw(): Promise<MyPostRow[]> {
    try {
        const res = await privateInstance.get('/likes/myposts');
        const data = (res.data?.data ?? res.data) as unknown;
        return Array.isArray(data) ? (data as MyPostRow[]) : [];
    } catch {
        return [];
    }
}

export async function fetchUserProfileById(
    id: number
): Promise<{ profileImageUrl?: string | null } | null> {
    try {
        const res = await privateInstance.get(`/users/by-id/${id}`);
        const data = (res.data?.data ?? res.data) as {
            profileImageUrl?: string | null;
        } | null;
        return data ?? null;
    } catch {
        return null;
    }
}
