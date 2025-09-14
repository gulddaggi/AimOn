export type UserProfile = {
    id: number;
    nickname: string;
    email?: string | null;
    profileImageUrl?: string | null;
    level: number;
    exp: number;
};
