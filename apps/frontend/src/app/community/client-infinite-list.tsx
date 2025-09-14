'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import { CommunityPost } from '@/types/community';
import CommunityPostCard from '@/components/organisms/community/community-post-card';
import { useRouter } from 'next/navigation';
import { setLastPost } from '@/lib/community-route';
// mock 로컬 오버라이드 제거

type Props = {
    initialPosts: CommunityPost[];
    pageSize: number;
    total: number;
    sortBy: 'latest' | 'popular';
    search?: string;
};

export default function ClientInfiniteList({
    initialPosts,
    pageSize,
    total,
    sortBy,
    search = '',
}: Props) {
    const [items, setItems] = useState<CommunityPost[]>(initialPosts);
    const [cursor, setCursor] = useState<number>(initialPosts.length);
    const [loading, setLoading] = useState(false);
    const [ended, setEnded] = useState(initialPosts.length >= total);
    const sentinelRef = useRef<HTMLDivElement | null>(null);
    const router = useRouter();

    const canLoad = useMemo(() => !loading && !ended, [loading, ended]);

    useEffect(() => {
        setItems(initialPosts);
        // 초기 커서/종료 상태도 함께 재설정
        setCursor(initialPosts.length);
        setEnded(initialPosts.length >= total);
        // 의존성에 initialPosts, total 포함하여 경고 제거
    }, [initialPosts, total]);

    // 서버 응답에 likeCount/commentCount가 누락된 경우 상세 조회로 보강
    useEffect(() => {
        const enrichCountsIfMissing = async () => {
            const missing = items.filter(
                p => p.likeCount === undefined || p.commentCount === undefined
            );
            if (missing.length === 0) return;
            try {
                const details = await Promise.all(
                    missing.map(p =>
                        fetch(`/next-api/community/post/${p.id}`, {
                            cache: 'no-store',
                        }).then(r => r.json())
                    )
                );
                const byId = new Map<
                    string,
                    { likeCount?: number; commentCount?: number }
                >();
                details.forEach(d => {
                    const id = String(d.postId ?? '');
                    if (!id) return;
                    byId.set(id, {
                        likeCount:
                            typeof d.likeCount === 'number' ? d.likeCount : 0,
                        commentCount:
                            typeof d.commentCount === 'number'
                                ? d.commentCount
                                : 0,
                    });
                });
                setItems(prev =>
                    prev.map(p => {
                        const patch = byId.get(p.id);
                        if (!patch) return p;
                        const next: CommunityPost = { ...p } as CommunityPost;
                        if (typeof next.likeCount !== 'number') {
                            next.likeCount = patch.likeCount ?? 0;
                        }
                        if (typeof next.commentCount !== 'number') {
                            next.commentCount = patch.commentCount ?? 0;
                        }
                        // 세션에 저장된 즉시 토글 결과 반영
                        try {
                            if (typeof window !== 'undefined') {
                                const raw = sessionStorage.getItem(
                                    `likes:${p.id}`
                                );
                                if (raw) {
                                    const cached: {
                                        liked?: boolean;
                                        likeCount?: number;
                                    } = JSON.parse(raw);
                                    if (typeof cached.liked === 'boolean') {
                                        (
                                            next as unknown as CommunityPost
                                        ).liked = cached.liked;
                                    }
                                    if (typeof cached.likeCount === 'number') {
                                        next.likeCount = cached.likeCount;
                                    }
                                }
                            }
                        } catch {}
                        return next;
                    })
                );
            } catch {
                // ignore
            }
        };
        enrichCountsIfMissing();
    }, [items]);

    useEffect(() => {
        if (!sentinelRef.current) return;
        const io = new IntersectionObserver(
            async entries => {
                const [entry] = entries;
                if (!entry.isIntersecting || !canLoad) return;
                setLoading(true);
                try {
                    const res = await fetch(
                        `/next-api/community/posts?cursor=${cursor}&limit=${pageSize}&sortBy=${sortBy}&q=${encodeURIComponent(search)}`,
                        { cache: 'no-store' }
                    );

                    const data: {
                        items: CommunityPost[];
                        nextCursor: number | null;
                    } = await res.json();
                    setItems(prev => [...prev, ...data.items]);
                    if (data.nextCursor === null) setEnded(true);
                    else setCursor(data.nextCursor);
                } finally {
                    setLoading(false);
                }
            },
            { rootMargin: '300px 0px' }
        );

        io.observe(sentinelRef.current);
        return () => io.disconnect();
    }, [cursor, pageSize, canLoad, sortBy, search]);

    useEffect(() => {
        let cancelled = false;
        const reload = async () => {
            setLoading(true);
            try {
                const res = await fetch(
                    `/next-api/community/posts?cursor=0&limit=${pageSize}&sortBy=${sortBy}&q=${encodeURIComponent(search)}`,
                    { cache: 'no-store' }
                );
                const data: {
                    items: CommunityPost[];
                    nextCursor: number | null;
                } = await res.json();
                if (cancelled) return;
                setItems(data.items);
                setCursor(data.nextCursor ?? data.items.length);
                setEnded(data.nextCursor === null);
            } finally {
                if (!cancelled) setLoading(false);
            }
        };
        reload();
        return () => {
            cancelled = true;
        };
    }, [sortBy, pageSize, search]);

    return (
        <div className="communityList">
            {items.map((p, index) => (
                <div key={p.id}>
                    <div
                        className="communityCardButton"
                        role="button"
                        tabIndex={0}
                        onClick={() => {
                            setLastPost(p);
                            router.push(`/community/${p.id}`);
                        }}
                        onKeyDown={e => {
                            if (e.key === 'Enter' || e.key === ' ') {
                                setLastPost(p);
                                router.push(`/community/${p.id}`);
                            }
                        }}
                    >
                        <CommunityPostCard post={p} />
                    </div>
                    {index < items.length - 1 && (
                        <div className="communityDivider" />
                    )}
                </div>
            ))}

            {/* 센티넬. 무한 스크롤 마지막 감지*/}
            <div ref={sentinelRef} />
            {loading && <div className="communityLoading">로딩 중...</div>}
            {ended && (
                <div className="communityEnded">모든 글을 불러왔습니다.</div>
            )}
        </div>
    );
}
