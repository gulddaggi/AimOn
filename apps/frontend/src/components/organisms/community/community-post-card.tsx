'use client';

import Image from 'next/image';
import profileFallback from '@/resources/community/resource-profile.png';
import commentIcon from '@/resources/resource-comment-white.svg';
import likeIcon from '@/resources/resource-like-white.svg';
import likeRedIcon from '@/resources/community/resource-like-red.svg';
import { CommunityPost } from '@/types/community';
import IconButton from '@/components/atoms/buttons/icon-button';
import { ImageElement } from '@/types/image';
import { useEffect, useMemo, useState } from 'react';
import { usePresignedImage } from '@/lib/profile/use-presigned-image';

function formatDateTime(iso: string) {
    const d = new Date(iso);
    const adj = new Date(d.getTime() + 9 * 60 * 60 * 1000);
    const y = adj.getFullYear();
    const m = String(adj.getMonth() + 1).padStart(2, '0');
    const day = String(adj.getDate()).padStart(2, '0');
    const hh = String(adj.getHours()).padStart(2, '0');
    const mm = String(adj.getMinutes()).padStart(2, '0');
    return `${y}.${m}.${day} ${hh}:${mm}`;
}

type Props = {
    post: CommunityPost;
    onLikeToggled?: (args: {
        postId: string;
        liked: boolean;
        likeCount: number;
    }) => void;
};

export default function CommunityPostCard({ post, onLikeToggled }: Props) {
    const [liked, setLiked] = useState<boolean>(!!post.liked);
    const [likeCount, setLikeCount] = useState<number>(post.likeCount);
    const [toggling, setToggling] = useState<boolean>(false);
    const { src: presignedSrc } = usePresignedImage(
        post.authorProfileUrl ?? null
    );

    const likeDefault: ImageElement = useMemo(
        () => ({ src: likeIcon.src, width: 18, height: 18 }),
        []
    );
    const likeRed: ImageElement = useMemo(
        () => ({ src: likeRedIcon.src, width: 18, height: 18 }),
        []
    );

    useEffect(() => {
        // 서버에서 liked가 내려오면 초기 동기화
        setLiked(!!post.liked);
        setLikeCount(post.likeCount);
    }, [post.liked, post.likeCount]);

    const onToggleLike = async (e?: React.MouseEvent) => {
        e?.stopPropagation();
        if (toggling) return;
        setToggling(true);
        try {
            const res = await fetch(
                `/next-api/community/likes/posts/${post.id}`,
                { method: 'POST' }
            );
            const data = await res.json();
            const nextLiked =
                typeof data?.liked === 'boolean' ? data.liked : liked;
            const nextCount =
                typeof data?.likeCount === 'number'
                    ? data.likeCount
                    : likeCount;
            setLiked(nextLiked);
            setLikeCount(nextCount);
            if (onLikeToggled)
                onLikeToggled({
                    postId: post.id,
                    liked: nextLiked,
                    likeCount: nextCount,
                });
            try {
                if (typeof window !== 'undefined') {
                    sessionStorage.setItem(
                        `likes:${post.id}`,
                        JSON.stringify({
                            liked: !!data?.liked,
                            likeCount: Number(data?.likeCount ?? likeCount),
                        })
                    );
                }
            } catch {}
        } catch {
            // ignore
        } finally {
            setToggling(false);
        }
    };
    return (
        <article className="communityCard">
            <header className="communityCardHeader">
                <Image
                    src={presignedSrc || profileFallback}
                    alt={`${post.authorName} profile`}
                    width={36}
                    height={36}
                    className="communityProfile"
                />
                <div className="communityMeta">
                    <div className="communityAuthor">{post.authorName}</div>
                    <div className="communityDate">
                        {formatDateTime(post.createdAt)}
                    </div>
                </div>
            </header>

            <h3 className="communityTitle">{post.title}</h3>
            <p className="communityBody">{post.content}</p>

            <footer className="communityFooter">
                <div className="communityStat">
                    <Image
                        src={commentIcon}
                        alt="댓글"
                        width={18}
                        height={18}
                    />
                    <span>{Intl.NumberFormat().format(post.commentCount)}</span>
                </div>
                <div className="communityStat">
                    <IconButton
                        className=""
                        image={liked ? likeRed : likeDefault}
                        alt="좋아요"
                        func={onToggleLike}
                    />
                    <span>{Intl.NumberFormat().format(likeCount)}</span>
                </div>
            </footer>
        </article>
    );
}
