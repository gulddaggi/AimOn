'use client';

import { useRouter } from 'next/navigation';
import TextInput from '@/components/atoms/inputs/text-input';
import CommunityHeader from '@/components/molecules/community-header';
import { useState } from 'react';
import Toast from '@/components/molecules/toast';

export default function CommunityWriteTemplate() {
    const router = useRouter();
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [toastMsg, setToastMsg] = useState<string | null>(null);

    const handleSubmit = async () => {
        try {
            const res = await fetch('/next-api/community/posts', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title, body: content }),
            });
            if (!res.ok) throw new Error('failed');
            setToastMsg('게시글이 등록되었습니다.');
            window.scrollTo({ top: 0, behavior: 'smooth' });
            setTimeout(() => {
                router.push('/community?created=1');
            }, 500);
        } catch {
            setToastMsg('등록에 실패했습니다.');
        }
    };

    return (
        <>
            <section className="communityPage communityWritePage">
                <div
                    style={{
                        paddingTop: 24,
                        paddingLeft: 32,
                        paddingRight: 32,
                        paddingBottom: 12,
                    }}
                >
                    <CommunityHeader title="커뮤니티 / 게시글 작성" showBack />
                </div>

                <div
                    className="writeForm"
                    style={{ paddingLeft: 32, paddingRight: 32 }}
                >
                    <div className="formField">
                        <label className="fieldLabel">제목</label>
                        <TextInput
                            className="writeTitleInput"
                            name="title"
                            type="text"
                            placeholder="제목을 입력하세요."
                            value={title}
                            onChange={v => setTitle(v)}
                        />
                    </div>

                    <div className="formField">
                        <label className="fieldLabel">내용</label>
                        <textarea
                            className="communityTextarea"
                            placeholder="내용을 입력하세요."
                            value={content}
                            onChange={e => setContent(e.target.value)}
                        />
                    </div>

                    <div className="formActions">
                        <button className="submitButton" onClick={handleSubmit}>
                            등록
                        </button>
                    </div>
                </div>
            </section>
            {toastMsg ? (
                <Toast message={toastMsg} onDone={() => setToastMsg(null)} />
            ) : null}
        </>
    );
}
