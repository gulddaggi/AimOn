'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import {
    deletePost,
    getPostDetail,
    getCommentsByPost,
} from '@/lib/axios/post/post-api';
import { getLastPost } from '@/lib/community-route';
import CommunityHeader from '@/components/molecules/community-header';
import CommunityPostCard from '@/components/organisms/community/community-post-card';
import Image from 'next/image';
import editIcon from '@/resources/resource-edit-white.svg';
import deleteIcon from '@/resources/community/resource-delete-white.svg';
import CommentCard, {
    BackendComment,
} from '@/components/molecules/comment-card';
import Toast from '@/components/molecules/toast';

// mock 제거

export default function CommunityDetailTemplate() {
    const router = useRouter();
    const params = useParams();
    const [post, setPost] = useState(getLastPost());
    const [meNickname, setMeNickname] = useState<string | null>(null);

    useEffect(() => {
        const id = String(params?.id ?? '');
        if (!id) return;
        // 상세 API 연동 (세션에 값이 있더라도 서버 최신으로 갱신)
        getPostDetail(id)
            .then(d => {
                setPost({
                    id: String(d.postId),
                    authorId: d.authorId,
                    authorName: d.authorNickname,
                    createdAt: d.createdAt,
                    title: d.title,
                    content: d.body,
                    authorProfileUrl: null,
                    commentCount: d.commentCount ?? 0,
                    likeCount: d.likeCount ?? 0,
                });
            })
            .catch(() => {});
        // 댓글 로드
        getCommentsByPost(id)
            .then(list => setComments(list))
            .catch(() => setComments([]));
        // 내 정보(닉네임 비교)
        fetch('/next-api/users/me')
            .then(r => (r.ok ? r.json() : null))
            .then(d => setMeNickname(d?.nickname ?? null))
            .catch(() => setMeNickname(null));
    }, [params]);

    const [replyOpenFor, setReplyOpenFor] = useState<number | null>(null);
    const [comments, setComments] = useState<BackendComment[]>([]);
    const [newComment, setNewComment] = useState('');
    const [newReply, setNewReply] = useState('');
    const [editOpenFor, setEditOpenFor] = useState<number | null>(null);
    const [editText, setEditText] = useState('');
    const [toastMsg, setToastMsg] = useState<string | null>(null);

    // 댓글 수 변경 시, 상세 카드의 commentCount를 로컬로 동기화
    useEffect(() => {
        const nextCount = comments.length;
        setPost(prev =>
            prev && prev.commentCount !== nextCount
                ? { ...prev, commentCount: nextCount }
                : prev
        );
    }, [comments.length]);

    if (!post) {
        return (
            <section className="communityPage" style={{ padding: 20 }}>
                <CommunityHeader title="커뮤니티 / 게시글 조회" showBack />
                <h1 className="communityPageTitle">
                    게시글을 찾을 수 없습니다.
                </h1>
                <button
                    className="communityCardButton"
                    onClick={() => router.push('/community')}
                >
                    목록으로
                </button>
            </section>
        );
    }

    const roots = comments.filter(c => c.parentCommentId === null);
    const childrenOf = (pid: number) =>
        comments.filter(c => c.parentCommentId === pid);

    const onAddComment = async () => {
        if (!newComment.trim() || !post) return;
        try {
            const created = await (
                await fetch('/next-api/community/comments', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        postId: Number(post.id),
                        parentCommentId: null,
                        content: newComment,
                    }),
                })
            ).json();
            setComments(prev => [...prev, created]);
            setNewComment('');
        } catch {}
    };

    const onAddReply = async (parentId: number) => {
        if (!newReply.trim() || !post) return;
        try {
            const created = await (
                await fetch('/next-api/community/comments', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        postId: Number(post.id),
                        parentCommentId: parentId,
                        content: newReply,
                    }),
                })
            ).json();
            setComments(prev => [...prev, created]);
            setNewReply('');
            setReplyOpenFor(null);
        } catch {}
    };

    const onDelete = async (id: number) => {
        if (!window.confirm('정말 삭제하시겠습니까?')) return;
        try {
            await fetch(`/next-api/community/comments/${id}`, {
                method: 'DELETE',
            });
            setComments(prev =>
                prev.filter(c => c.id !== id && c.parentCommentId !== id)
            );
            setToastMsg('삭제가 완료되었습니다.');
        } catch {}
    };

    const onEdit = (id: number) => {
        setEditOpenFor(prev => (prev === id ? null : id));
        const target = comments.find(c => c.id === id);
        setEditText(target ? target.content : '');
    };

    const onSubmitEdit = async (id: number) => {
        if (!editText.trim()) return;
        try {
            await fetch(`/next-api/community/comments/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(editText),
            });
            setComments(prev =>
                prev.map(c => (c.id === id ? { ...c, content: editText } : c))
            );
            setEditOpenFor(null);
            setEditText('');
            setToastMsg('수정이 완료되었습니다.');
        } catch {}
    };

    const canModifyPost = meNickname !== null && meNickname === post.authorName;

    return (
        <section className="communityPage communityDetailPage">
            <div className="communityStickyInner">
                <CommunityHeader title="커뮤니티 / 게시글 조회" showBack />
            </div>

            <div className="communityBodyInner">
                {canModifyPost ? (
                    <div
                        className="postActions"
                        style={{
                            display: 'flex',
                            justifyContent: 'flex-end',
                            gap: 8,
                            alignItems: 'center',
                        }}
                    >
                        <button
                            className="textButton"
                            onClick={() =>
                                router.push(`/community/${post.id}/edit`)
                            }
                            style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: 6,
                                color: '#ffffff',
                            }}
                        >
                            <Image
                                src={editIcon}
                                alt="편집"
                                width={18}
                                height={18}
                            />{' '}
                            편집
                        </button>
                        <button
                            className="textButton"
                            onClick={async () => {
                                if (!window.confirm('정말 삭제하시겠습니까?'))
                                    return;
                                try {
                                    await deletePost(String(post.id));
                                    router.replace('/community?toast=deleted');
                                } catch {}
                            }}
                            style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: 6,
                                color: '#ffffff',
                            }}
                        >
                            <Image
                                src={deleteIcon}
                                alt="삭제"
                                width={18}
                                height={18}
                            />{' '}
                            삭제
                        </button>
                    </div>
                ) : null}

                <div style={{ marginTop: 8 }}>
                    <CommunityPostCard post={post} />
                </div>

                {/* 댓글 입력 */}
                <div className="commentEditor" style={{ marginTop: 16 }}>
                    <textarea
                        className="communityTextarea"
                        placeholder="댓글을 입력하세요."
                        value={newComment}
                        onChange={e => setNewComment(e.target.value)}
                    />
                    <div
                        className="formActions"
                        style={{ display: 'flex', justifyContent: 'flex-end' }}
                    >
                        <button
                            className="submitButton primaryActionButton"
                            onClick={onAddComment}
                        >
                            등록
                        </button>
                    </div>
                </div>

                {/* 댓글 목록 */}
                <div className="commentList" style={{ marginTop: 12 }}>
                    {roots.map(c => (
                        <div
                            key={c.id}
                            className="commentItem"
                            style={{ marginBottom: 16 }}
                        >
                            <CommentCard
                                comment={c}
                                depth={0}
                                onReply={id =>
                                    setReplyOpenFor(prev =>
                                        prev === id ? null : id
                                    )
                                }
                                onEdit={onEdit}
                                onDelete={onDelete}
                                canModify={meNickname === c.authorNickname}
                            />
                            {/* 대댓글 입력 */}
                            {replyOpenFor === c.id && (
                                <div
                                    className="replyEditor"
                                    style={{ marginTop: 8, marginLeft: 24 }}
                                >
                                    <textarea
                                        className="communityTextarea"
                                        placeholder="답글을 입력하세요."
                                        value={newReply}
                                        onChange={e =>
                                            setNewReply(e.target.value)
                                        }
                                    />
                                    <div
                                        className="formActions"
                                        style={{
                                            display: 'flex',
                                            justifyContent: 'flex-end',
                                        }}
                                    >
                                        <button
                                            className="submitButton primaryActionButton"
                                            onClick={() => onAddReply(c.id)}
                                        >
                                            등록
                                        </button>
                                    </div>
                                </div>
                            )}

                            {/* 수정 입력 */}
                            {editOpenFor === c.id && (
                                <div
                                    className="replyEditor"
                                    style={{ marginTop: 8, marginLeft: 24 }}
                                >
                                    <textarea
                                        className="communityTextarea"
                                        placeholder="내용을 수정하세요."
                                        value={editText}
                                        onChange={e =>
                                            setEditText(e.target.value)
                                        }
                                    />
                                    <div
                                        className="formActions"
                                        style={{
                                            display: 'flex',
                                            justifyContent: 'flex-end',
                                        }}
                                    >
                                        <button
                                            className="submitButton primaryActionButton"
                                            onClick={() => onSubmitEdit(c.id)}
                                        >
                                            수정
                                        </button>
                                    </div>
                                </div>
                            )}

                            {/* 대댓글 목록 */}
                            {childrenOf(c.id).map(r => (
                                <div
                                    key={r.id}
                                    className="replyItem"
                                    style={{ marginTop: 8, marginLeft: 24 }}
                                >
                                    <CommentCard
                                        comment={r}
                                        depth={1}
                                        onEdit={onEdit}
                                        onDelete={onDelete}
                                        canModify={
                                            meNickname === r.authorNickname
                                        }
                                    />
                                    {editOpenFor === r.id && (
                                        <div
                                            className="replyEditor"
                                            style={{
                                                marginTop: 8,
                                                marginLeft: 24,
                                            }}
                                        >
                                            <textarea
                                                className="communityTextarea"
                                                placeholder="내용을 수정하세요."
                                                value={editText}
                                                onChange={e =>
                                                    setEditText(e.target.value)
                                                }
                                            />
                                            <div
                                                className="formActions"
                                                style={{
                                                    display: 'flex',
                                                    justifyContent: 'flex-end',
                                                }}
                                            >
                                                <button
                                                    className="submitButton primaryActionButton"
                                                    onClick={() =>
                                                        onSubmitEdit(r.id)
                                                    }
                                                >
                                                    수정
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            ))}
                            <div className="communityDivider" />
                        </div>
                    ))}
                </div>
                {toastMsg ? (
                    <Toast
                        message={toastMsg}
                        onDone={() => setToastMsg(null)}
                    />
                ) : null}
            </div>
        </section>
    );
}
