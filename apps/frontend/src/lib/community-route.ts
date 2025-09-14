import { CommunityPost } from '@/types/community';

const KEY = 'community:lastPost';

export function setLastPost(post: CommunityPost) {
    if (typeof window === 'undefined') return;
    try {
        sessionStorage.setItem(KEY, JSON.stringify(post));
    } catch {}
}

export function getLastPost(): CommunityPost | null {
    if (typeof window === 'undefined') return null;
    try {
        const raw = sessionStorage.getItem(KEY);
        if (!raw) return null;
        return JSON.parse(raw) as CommunityPost;
    } catch {
        return null;
    }
}
