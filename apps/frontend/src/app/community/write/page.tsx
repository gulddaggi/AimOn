import CommunityWriteTemplate from '@/components/templates/community-write-template';

export default async function CommunityWritePage() {
    // 서버 컴포넌트: 추후 SSR에 필요한 초기 데이터(fetch 등) 주입 가능
    return <CommunityWriteTemplate />;
}
