export type CommunityPost = {
    id: string;
    authorId?: number;
    authorName: string;
    authorProfileUrl?: string | null;
    createdAt: string;
    title: string;
    content: string;
    commentCount: number;
    likeCount: number;
    liked?: boolean;
};
