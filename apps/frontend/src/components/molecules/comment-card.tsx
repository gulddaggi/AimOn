'use client';

import Image from 'next/image';
import profileFallback from '@/resources/community/resource-profile.png';
import { usePresignedImage } from '@/lib/profile/use-presigned-image';

export type BackendComment = {
    id: number;
    postId: number;
    parentCommentId: number | null;
    content: string;
    authorNickname: string;
    authorId?: number;
    authorProfileUrl: string | null;
    createdAt: string;
};

type Props = {
    comment: BackendComment;
    depth?: number; // 0: 댓글, 1: 대댓글
    onReply?: (id: number) => void;
    onEdit?: (id: number) => void;
    onDelete?: (id: number) => void;
    canModify?: boolean;
};

export default function CommentCard({
    comment,
    depth = 0,
    onReply,
    onEdit,
    onDelete,
    canModify = true,
}: Props) {
    const { id, content, authorNickname, authorProfileUrl, createdAt } =
        comment;
    const { src: avatarSrc } = usePresignedImage(authorProfileUrl ?? null);
    return (
        <div
            className="commentCard"
            style={{ marginLeft: depth === 0 ? 0 : 24 }}
        >
            <div className="commentHeader">
                <div className="commentHeaderLeft">
                    <Image
                        src={avatarSrc || profileFallback}
                        alt={`${authorNickname} profile`}
                        width={36}
                        height={36}
                        className="commentAvatar"
                    />
                    <div className="commentMetaRow">
                        <div className="commentName">{authorNickname}</div>
                        <div className="commentDate">
                            {(() => {
                                const d = new Date(createdAt);
                                const adj = new Date(
                                    d.getTime() + 9 * 60 * 60 * 1000
                                );
                                const y = adj.getFullYear();
                                const m = String(adj.getMonth() + 1).padStart(
                                    2,
                                    '0'
                                );
                                const day = String(adj.getDate()).padStart(
                                    2,
                                    '0'
                                );
                                const hh = String(adj.getHours()).padStart(
                                    2,
                                    '0'
                                );
                                const mm = String(adj.getMinutes()).padStart(
                                    2,
                                    '0'
                                );
                                return `${y}.${m}.${day} ${hh}:${mm}`;
                            })()}
                        </div>
                    </div>
                </div>
                <div className="commentActions">
                    {depth === 0 && onReply && (
                        <button
                            className="textButton"
                            onClick={() => onReply(id)}
                            style={{ color: '#ffffff' }}
                        >
                            답글
                        </button>
                    )}
                    {canModify && onEdit && (
                        <button
                            className="textButton"
                            onClick={() => onEdit(id)}
                            style={{ color: '#ffffff' }}
                        >
                            수정
                        </button>
                    )}
                    {canModify && onDelete && (
                        <button
                            className="textButton"
                            onClick={() => onDelete(id)}
                            style={{ color: '#ffffff' }}
                        >
                            삭제
                        </button>
                    )}
                </div>
            </div>
            <div className="commentBody">
                <div className="commentContent">{content}</div>
            </div>
        </div>
    );
}
