'use client';

import { CommunityPost } from '@/types/community';
import CommunityPostCard from './community-post-card';
import { useRouter } from 'next/navigation';
import { useMemo } from 'react';

type Props = {
    title: string;
    posts: CommunityPost[];
    onLikeToggled?: (args: {
        postId: string;
        liked: boolean;
        likeCount: number;
    }) => void;
};

export default function MyPostsList({ title, posts, onLikeToggled }: Props) {
    const router = useRouter();
    const items = useMemo(() => posts, [posts]);
    return (
        <section className="myPostsSection">
            <h2 className="sectionTitle">{title}</h2>
            <div className="myPostsCard">
                {items.map((p, index) => (
                    <div key={p.id}>
                        <div
                            className="communityCardButton"
                            role="button"
                            tabIndex={0}
                            onClick={() => router.push(`/community/${p.id}`)}
                        >
                            <CommunityPostCard
                                post={p}
                                onLikeToggled={onLikeToggled}
                            />
                        </div>
                        {index < items.length - 1 && (
                            <div className="communityDivider" />
                        )}
                    </div>
                ))}
            </div>
        </section>
    );
}
