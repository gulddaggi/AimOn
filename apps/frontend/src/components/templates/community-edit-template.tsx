'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import CommunityHeader from '@/components/molecules/community-header';
import { getPostDetail, updatePost } from '@/lib/axios/post/post-api';
import { getLastPost } from '@/lib/community-route';

export default function CommunityEditTemplate() {
    const params = useParams();
    const router = useRouter();
    const [post, setPost] = useState(getLastPost());

    useEffect(() => {
        const id = String(params?.id ?? '');
        if (!id) return;
        getPostDetail(id)
            .then(d => {
                setPost({
                    id: String(d.postId),
                    authorName: d.authorNickname,
                    createdAt: d.createdAt,
                    title: d.title,
                    content: d.body,
                    authorProfileUrl: null,
                    commentCount: d.commentCount ?? 0,
                    likeCount: d.likeCount ?? 0,
                });
                setTitle(d.title);
                setContent(d.body);
            })
            .catch(() => {});
    }, [params]);

    const [title, setTitle] = useState(post?.title ?? '');
    const [content, setContent] = useState(post?.content ?? '');

    if (!post) {
        return (
            <section className="communityPage" style={{ padding: 20 }}>
                <CommunityHeader title="커뮤니티 / 게시글 수정" showBack />
                <h1 className="communityPageTitle">
                    게시글을 찾을 수 없습니다.
                </h1>
            </section>
        );
    }

    const onSubmit = async () => {
        if (!post) return;
        await updatePost(post.id, { title, body: content });
        router.replace(`/community/${post.id}`);
    };

    return (
        <section className="communityPage communityEditPage">
            <div className="communityStickyInner">
                <CommunityHeader title="커뮤니티 / 게시글 수정" showBack />
            </div>

            <div className="communityBodyInner">
                <div className="writeForm" style={{ gap: 16 }}>
                    <div
                        className="formField"
                        style={{ display: 'flex', flexDirection: 'column' }}
                    >
                        <label className="fieldLabel">제목</label>
                        <input
                            className="textInput writeTitleInput"
                            value={title}
                            onChange={e => setTitle(e.target.value)}
                            placeholder="제목을 입력하세요."
                        />
                    </div>
                    <div className="formField">
                        <label className="fieldLabel">내용</label>
                        <textarea
                            className="communityTextarea"
                            value={content}
                            onChange={e => setContent(e.target.value)}
                            placeholder="내용을 입력하세요."
                        />
                    </div>
                    <div
                        className="formActions"
                        style={{ display: 'flex', justifyContent: 'flex-end' }}
                    >
                        <button
                            className="submitButton primaryActionButton"
                            onClick={onSubmit}
                        >
                            수정
                        </button>
                    </div>
                </div>
            </div>
        </section>
    );
}
