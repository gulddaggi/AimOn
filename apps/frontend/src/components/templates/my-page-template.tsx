'use client';

import CommunityHeader from '@/components/molecules/community-header';
import ProfileCard from '@/components/molecules/profile-card';
import MyPostsList from '@/components/organisms/community/my-posts-list';
import type { CommunityPost } from '@/types/community';
import { useState, useCallback } from 'react';
import { UserProfile } from '@/types/user';

export default function MyPageTemplate({
    profile,
    myPosts,
    likedPosts,
}: {
    profile: UserProfile | null;
    myPosts: CommunityPost[];
    likedPosts: CommunityPost[];
}) {
    // 서버에서 받아온 프로필이 있으면 그 값 사용, 없으면 임시값
    const nickname = profile?.nickname ?? '';
    const email = profile?.email ?? '';
    const level = profile?.level ?? 1;
    const exp = profile?.exp ?? 80;
    const expMax = 100;

    const [myPostsState, setMyPostsState] = useState<CommunityPost[]>(myPosts);
    const [likedPostsState, setLikedPostsState] =
        useState<CommunityPost[]>(likedPosts);

    const handleLikeToggled = useCallback(
        (args: { postId: string; liked: boolean; likeCount: number }) => {
            setMyPostsState(prev =>
                prev.map(p => {
                    if (p.id === args.postId) {
                        return {
                            ...p,
                            liked: args.liked,
                            likeCount: args.likeCount,
                        };
                    }
                    return p;
                })
            );
            setLikedPostsState(prev => {
                const exists = prev.some(p => p.id === args.postId);
                if (args.liked) {
                    if (exists)
                        return prev.map(p => {
                            if (p.id === args.postId) {
                                return {
                                    ...p,
                                    liked: true,
                                    likeCount: args.likeCount,
                                };
                            }
                            return p;
                        });
                    const found = myPostsState.find(p => p.id === args.postId);
                    const base = found ?? {
                        id: args.postId,
                        authorName: '',
                        createdAt: new Date().toISOString(),
                        title: '',
                        content: '',
                        commentCount: 0,
                        likeCount: args.likeCount,
                    };
                    return [
                        { ...base, liked: true, likeCount: args.likeCount },
                        ...prev,
                    ];
                }
                return prev.filter(p => p.id !== args.postId);
            });
        },
        [myPostsState]
    );

    return (
        <section className="communityPage">
            <div className="communityStickyInner" style={{ paddingBottom: 16 }}>
                <CommunityHeader title="마이페이지" />
            </div>

            <div className="communityBodyInner">
                <ProfileCard
                    nickname={nickname}
                    email={email}
                    level={level}
                    exp={exp}
                    expMax={expMax}
                    profileImageUrl={profile?.profileImageUrl ?? null}
                    userId={profile?.id}
                />

                <div className="myPageGrid">
                    <MyPostsList
                        title="나의 게시글"
                        posts={myPostsState}
                        onLikeToggled={handleLikeToggled}
                    />
                    <MyPostsList
                        title="좋아요 한 글"
                        posts={likedPostsState}
                        onLikeToggled={handleLikeToggled}
                    />
                </div>
            </div>
        </section>
    );
}
