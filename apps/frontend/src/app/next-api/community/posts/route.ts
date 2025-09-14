import { NextRequest, NextResponse } from 'next/server';
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

type BackendUserById = {
    id: number;
    nickname: string;
    email?: string | null;
    profileImageUrl?: string | null;
    level?: number;
    exp?: number;
};

type BackendLikedPost = {
    postId: number;
};

// origin 계산 함수는 현재 사용하지 않음 (presigned URL 처리를 클라이언트 훅으로 이동)

// getDownloadUrlForKey: 사용하지 않음 (클라이언트 훅에서 presigned 처리)

export async function GET(req: NextRequest) {
    const { searchParams } = new URL(req.url);
    const cursor = Number(searchParams.get('cursor') ?? 0);
    const limit = Number(searchParams.get('limit') ?? 10);
    const sortBy = (searchParams.get('sortBy') || 'latest') as
        | 'latest'
        | 'popular';
    const q = (searchParams.get('q') || '').toLowerCase().trim();

    try {
        // 목록은 인증이 걸려 있을 수 있으므로 privateInstance 사용
        const res = await privateInstance.get('/posts');
        let raw: BackendPostListItem[] = [];
        if (Array.isArray(res.data)) {
            raw = res.data as BackendPostListItem[];
        } else if (
            Array.isArray((res as { data?: { data?: unknown } }).data?.data)
        ) {
            const wrapped = (res as { data?: { data?: unknown } }).data?.data;
            raw = (wrapped as BackendPostListItem[]) ?? [];
        }

        type BaseListItem = {
            id: string;
            authorId: number;
            authorName: string;
            createdAt: string;
            title: string;
        };
        const baseList: BaseListItem[] = raw.map(
            (listItem: BackendPostListItem) => ({
                id: String(listItem.postId),
                authorId: listItem.authorId ?? 0,
                authorName: listItem.authorNickname ?? '',
                createdAt: listItem.createdAt,
                title: listItem.title,
            })
        );

        // 검색 (제목/작성자)
        let filteredList: BaseListItem[] = baseList;
        if (q) {
            filteredList = baseList.filter((postRow: BaseListItem) =>
                `${postRow.title} ${postRow.authorName}`
                    .toLowerCase()
                    .includes(q)
            );
        }

        // 정렬 (목록 기준)
        filteredList.sort((left: BaseListItem, right: BaseListItem) => {
            if (sortBy === 'latest') {
                return (
                    new Date(right.createdAt).getTime() -
                    new Date(left.createdAt).getTime()
                );
            }
            return 0;
        });

        const total = filteredList.length;
        const start = cursor;
        const end = Math.min(cursor + limit, total);
        const windowItems = filteredList.slice(start, end);

        // 내가 좋아요한 글 목록 조회하여 liked 표시
        let myLikedSet = new Set<string>();
        try {
            const likedRes = await privateInstance.get('/likes/myposts');
            let likedRaw: BackendLikedPost[] = [];
            if (Array.isArray(likedRes.data)) {
                likedRaw = likedRes.data as BackendLikedPost[];
            } else if (Array.isArray(likedRes.data?.data)) {
                likedRaw = likedRes.data.data as BackendLikedPost[];
            }
            myLikedSet = new Set(likedRaw.map(l => String(l.postId)));
        } catch {}

        // 상세/작성자 프로필 보강 (현재 페이지 범위)
        const enriched: CommunityPost[] = await Promise.all(
            windowItems.map(async (listRow: BaseListItem) => {
                // 상세도 동일하게 인증 헤더 포함하여 호출
                const detailRes = await privateInstance.get(
                    `/posts/${listRow.id}`
                );
                const detailData = (detailRes.data?.data ??
                    detailRes.data) as BackendPostDetail;

                let authorProfileUrl: string | null = null;
                try {
                    // 프론트 프록시를 거쳐 요청하면 쿠키/토큰 일관성 유지
                    const userProxy = await fetch(
                        `${req.nextUrl.origin}/next-api/community/users/by-id/${detailData.authorId}`,
                        { cache: 'no-store' }
                    );
                    const userData =
                        (await userProxy.json()) as BackendUserById;
                    const key = userData.profileImageUrl ?? null;
                    if (key) {
                        // presigned를 서버에서 만들지 않고 키를 그대로 내려 카드 훅에서 변환
                        authorProfileUrl = String(key).replace(/^"|"$/g, '');
                    }
                } catch {
                    authorProfileUrl = null;
                }

                return {
                    id: String(detailData.postId),
                    authorName: detailData.authorNickname ?? listRow.authorName,
                    authorProfileUrl,
                    createdAt: detailData.createdAt ?? listRow.createdAt,
                    title: detailData.title,
                    content: detailData.body,
                    commentCount: Number(detailData.commentCount ?? 0),
                    likeCount: Number(detailData.likeCount ?? 0),
                    liked: myLikedSet.has(String(detailData.postId)),
                } as CommunityPost;
            })
        );

        let finalItems: CommunityPost[] = enriched;
        if (sortBy === 'popular') {
            finalItems = [...enriched].sort(
                (left: CommunityPost, right: CommunityPost) =>
                    right.likeCount - left.likeCount
            );
        }
        const nextCursor = end < total ? end : null;

        return NextResponse.json({ items: finalItems, nextCursor, total });
    } catch {
        return NextResponse.json(
            { items: [], nextCursor: null, total: 0 },
            { status: 200 }
        );
    }
}

export async function POST(req: NextRequest) {
    try {
        const body = await req.json();
        const payload = { title: body?.title ?? '', body: body?.body ?? '' };
        const res = await privateInstance.post('/posts', payload, {
            headers: { 'Content-Type': 'application/json' },
        });
        return NextResponse.json(res.data ?? { ok: true });
    } catch {
        return NextResponse.json({ ok: false }, { status: 400 });
    }
}
