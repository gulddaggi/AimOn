import MyPageTemplate from '@/components/templates/my-page-template';
import { fetchMyProfile } from '@/lib/axios/profile/profile-api';
import { CommunityPost } from '@/types/community';
import {
    fetchLikedPostsRaw,
    fetchMyPostsRaw,
    fetchUserProfileById,
} from '@/lib/axios/community/my-page-api';

export default async function Page() {
    const profile = await fetchMyProfile();
    // 내가 작성한 게시글 (서버에서 privateInstance로 직접 호출)
    const myPostsRaw: unknown = await fetchMyPostsRaw();
    const myPosts: CommunityPost[] = (
        Array.isArray(myPostsRaw) ? (myPostsRaw as unknown[]) : []
    ).map((d: unknown) => {
        const row = d as {
            postId: number;
            authorId?: number;
            authorNickname?: string;
            createdAt?: string;
            title?: string;
            body?: string;
            likeCount?: number;
            commentCount?: number;
        };
        return {
            id: String(row.postId),
            authorId: Number(row.authorId ?? 0),
            authorName: String(row.authorNickname ?? ''),
            createdAt: String(row.createdAt ?? new Date().toISOString()),
            title: String(row.title ?? ''),
            content: String(row.body ?? ''),
            authorProfileUrl: null,
            commentCount: Number(row.commentCount ?? 0),
            likeCount: Number(row.likeCount ?? 0),
            liked: false,
        } as CommunityPost;
    });

    // 내가 좋아요한 게시글 (서버에서 privateInstance로 직접 호출)
    const likedRaw: unknown = await fetchLikedPostsRaw();
    const likedPosts: CommunityPost[] = (
        Array.isArray(likedRaw) ? (likedRaw as unknown[]) : []
    ).map((d: unknown) => {
        const row = d as {
            postId: number;
            authorId?: number;
            authorNickname?: string;
            createdAt?: string;
            title?: string;
            body?: string;
            likeCount?: number;
            commentCount?: number;
        };
        return {
            id: String(row.postId),
            authorId: Number(row.authorId ?? 0),
            authorName: String(row.authorNickname ?? ''),
            createdAt: String(row.createdAt ?? new Date().toISOString()),
            title: String(row.title ?? ''),
            content: String(row.body ?? ''),
            authorProfileUrl: null,
            commentCount: Number(row.commentCount ?? 0),
            likeCount: Number(row.likeCount ?? 0),
            liked: true,
        } as CommunityPost;
    });

    // 작성자 프로필 이미지 키 보강 (게시글 카드와 동일 로직 기반: 키를 내려서 훅에서 presigned 변환)
    const authorIds = Array.from(
        new Set([
            ...myPosts
                .map(p => p.authorId)
                .filter((v): v is number => typeof v === 'number'),
            ...likedPosts
                .map(p => p.authorId)
                .filter((v): v is number => typeof v === 'number'),
        ])
    );
    const idToProfileKey = new Map<number, string | null>();
    await Promise.all(
        authorIds.map(async id => {
            try {
                const data = await fetchUserProfileById(id);
                const keyRaw = data?.profileImageUrl ?? null;
                const key = keyRaw
                    ? String(keyRaw).replace(/^"|"$/g, '')
                    : null;
                idToProfileKey.set(id, key);
            } catch {
                idToProfileKey.set(id, null);
            }
        })
    );

    // 좋아요한 글 id를 기반으로 내 글에도 liked 표기 반영
    const likedIdSet = new Set(likedPosts.map(p => p.id));
    const myPostsWithProfile: CommunityPost[] = myPosts.map(p => ({
        ...p,
        liked: likedIdSet.has(p.id),
        authorProfileUrl: p.authorId
            ? (idToProfileKey.get(p.authorId) ?? null)
            : null,
    }));
    const likedPostsWithProfile: CommunityPost[] = likedPosts.map(p => ({
        ...p,
        authorProfileUrl: p.authorId
            ? (idToProfileKey.get(p.authorId) ?? null)
            : null,
    }));
    return (
        <div className="page">
            <MyPageTemplate
                profile={profile}
                myPosts={myPostsWithProfile}
                likedPosts={likedPostsWithProfile}
            />
        </div>
    );
}
