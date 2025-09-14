import { BackendComment } from '@/components/molecules/comment-card';

export type PostDetail = {
    postId: number;
    authorId: number;
    authorNickname: string;
    title: string;
    body: string;
    createdAt: string;
    likeCount: number;
    commentCount: number;
};

export async function getPostDetail(
    postId: string | number
): Promise<PostDetail> {
    const res = await fetch(`/next-api/community/post/${postId}`, {
        cache: 'no-store',
    });
    return (await res.json()) as PostDetail;
}

export async function deletePost(postId: string | number): Promise<void> {
    await fetch(`/next-api/community/post/${postId}`, { method: 'DELETE' });
}

export async function updatePost(
    postId: string | number,
    payload: { title: string; body: string }
): Promise<PostDetail> {
    const res = await fetch(`/next-api/community/post/${postId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    });
    return (await res.json()) as PostDetail;
}

export async function getCommentsByPost(
    postId: string | number
): Promise<BackendComment[]> {
    const res = await fetch(`/next-api/community/comments/post/${postId}`, {
        cache: 'no-store',
    });
    const data = await res.json();
    return (Array.isArray(data) ? data : []) as BackendComment[];
}

export async function createComment(payload: {
    postId: number;
    parentCommentId?: number | null;
    content: string;
}) {
    const res = await fetch(`/next-api/community/comments`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    });
    return await res.json();
}

export async function updateComment(commentId: number, content: string) {
    const res = await fetch(`/next-api/community/comments/${commentId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(content),
    });
    return await res.json();
}

export async function deleteComment(commentId: number) {
    await fetch(`/next-api/community/comments/${commentId}`, {
        method: 'DELETE',
    });
}
