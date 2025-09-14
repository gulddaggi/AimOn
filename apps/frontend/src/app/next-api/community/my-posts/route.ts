import { NextResponse } from 'next/server';
import { privateInstance } from '@/lib/axios/axios-instance';
import { CommunityPost } from '@/types/community';

type BackendPostListItem = {
    postId: number;
    authorId?: number;
    authorNickname?: string;
    title: string;
    createdAt: string;
};

type BackendPostDetail = {
    postId: number;
    authorId: number;
    authorNickname: string;
    title: string;
    body: string;
    createdAt: string;
    likeCount?: number;
    commentCount?: number;
};

type BackendUser = { id: number; nickname?: string };

export async function GET() {
    try {
        const meRes = await privateInstance.get('/users/me');
        const me = (meRes.data?.data ?? meRes.data) as BackendUser;
        const myId = Number((me as BackendUser)?.id ?? 0);

        const listRes = await privateInstance.get('/posts');
        let listRaw: BackendPostListItem[] = [];
        if (Array.isArray(listRes.data)) {
            listRaw = listRes.data as BackendPostListItem[];
        } else if (Array.isArray(listRes.data?.data)) {
            listRaw = listRes.data.data as BackendPostListItem[];
        }

        // 1차: 목록에서 내 글만 후보로 축소해 상세 호출 수 최소화
        const candidateIds: string[] = [];
        for (const row of listRaw) {
            const authorId =
                typeof row.authorId === 'number' ? row.authorId : -1;
            if (authorId === myId) {
                candidateIds.push(String(row.postId));
            }
        }

        // 2차: 상세 확인 및 카드 데이터 구성
        const myPosts: CommunityPost[] = [];
        for (const id of candidateIds) {
            try {
                const detailRes = await privateInstance.get(`/posts/${id}`);
                const d = (detailRes.data?.data ??
                    detailRes.data) as BackendPostDetail;
                if (Number(d.authorId) !== myId) continue;
                myPosts.push({
                    id: String(d.postId),
                    authorId: d.authorId,
                    authorName: d.authorNickname,
                    createdAt: d.createdAt,
                    title: d.title,
                    content: d.body,
                    authorProfileUrl: null,
                    commentCount: Number(d.commentCount ?? 0),
                    likeCount: Number(d.likeCount ?? 0),
                });
            } catch {
                // ignore item failure
            }
        }

        return NextResponse.json(myPosts);
    } catch {
        return NextResponse.json([], { status: 200 });
    }
}
