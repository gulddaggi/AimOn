import CommunityListTemplate from '@/components/templates/community-list-template';
import { CommunityPost } from '@/types/community';
import { headers } from 'next/headers';

export const revalidate = 0;

const PAGE_SIZE = 10;

export default async function communityPage() {
    // SSR: 첫 페이지 프리패치 (절대 URL 필요)
    const h = await headers();
    const host = h.get('x-forwarded-host') ?? h.get('host');
    const proto = h.get('x-forwarded-proto') ?? 'http';
    const fallbackOrigin = process.env.NEXT_PUBLIC_SITE_URL;
    const origin = host
        ? `${proto}://${host}`
        : (fallbackOrigin ?? 'http://localhost:3000');

    // 백엔드 GET /posts는 request param이 없으므로, 프리패치는 프록시 라우트에 커서 파라미터만 전달해 프론트에서 윈도우를 계산합니다
    const res = await fetch(
        `${origin}/next-api/community/posts?cursor=0&limit=${PAGE_SIZE}&sortBy=latest`,
        { cache: 'no-store' }
    );
    const data = (await res.json()) as {
        items: CommunityPost[];
        nextCursor: number | null;
        total: number;
    };
    const initialPosts = data.items ?? [];
    return (
        <div className="page">
            {/* 작성 완료 후 돌아왔을 때 노출할 토스트 (URL 파라미터 기반) */}
            <CommunityListTemplate
                initialPosts={initialPosts}
                pageSize={PAGE_SIZE}
                total={data.total ?? initialPosts.length}
            />
        </div>
    );
}
