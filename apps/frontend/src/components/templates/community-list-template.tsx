'use client';

import ClientInfiniteList from '@/app/community/client-infinite-list';
import ToggleButton from '../atoms/buttons/toggle-button';
import TextInput from '@/components/atoms/inputs/text-input';
import { useAppSelector } from '@/lib/redux/hooks';
import Image from 'next/image';
import addIcon from '@/resources/community/resource-community-add-white.svg';
import { useRouter } from 'next/navigation';
import { useEffect, useLayoutEffect, useRef } from 'react';
import CommunityHeader from '@/components/molecules/community-header';
import { useAppDispatch } from '@/lib/redux/hooks';
import { textUpdate } from '@/lib/redux/feature/input/input-text-slice';
import { CommunityPost } from '@/types/community';
import { useState } from 'react';
import Toast from '@/components/molecules/toast';

type Props = {
    initialPosts: CommunityPost[];
    pageSize: number;
    total: number;
};

export default function CommunityListTemplate(props: Props) {
    const [isLatest, setIsLatest] = useState(true);
    const router = useRouter();
    const dispatch = useAppDispatch();
    const search = useAppSelector(state => state.inputText.value);
    const [toastMsg, setToastMsg] = useState<string | null>(null);
    const stickyRef = useRef<HTMLDivElement | null>(null);
    const [stickyHeight, setStickyHeight] = useState(0);

    useEffect(() => {
        if (search) dispatch(textUpdate(''));
        if (typeof window !== 'undefined') {
            const params = new URLSearchParams(window.location.search);
            if (params.get('created') === '1') {
                setToastMsg('게시글이 등록되었습니다.');
                params.delete('created');
                const url = `${window.location.pathname}?${params.toString()}`;
                window.history.replaceState(
                    {},
                    '',
                    url.endsWith('?') ? url.slice(0, -1) : url
                );
            } else if (params.get('toast') === 'deleted') {
                setToastMsg('게시글이 삭제되었습니다.');
                params.delete('toast');
                const url = `${window.location.pathname}?${params.toString()}`;
                window.history.replaceState(
                    {},
                    '',
                    url.endsWith('?') ? url.slice(0, -1) : url
                );
            }
        }
    }, [dispatch, search]);

    useLayoutEffect(() => {
        const measure = () => {
            setStickyHeight(stickyRef.current?.offsetHeight ?? 0);
        };
        measure();
        window.addEventListener('resize', measure);
        return () => window.removeEventListener('resize', measure);
    }, []);

    return (
        <section className="communityPage">
            <div className="communitySticky" ref={stickyRef}>
                <div className="communityStickyInner">
                    <CommunityHeader title="커뮤니티" />

                    <div className="communityToolbar">
                        <div className="tagGroup">
                            <span
                                className={`sortLabel ${isLatest ? 'active' : ''}`}
                            >
                                최신
                            </span>
                            <ToggleButton
                                className="sortToggle"
                                isToggled={!isLatest}
                                func={() => setIsLatest(prev => !prev)}
                            />
                            <span
                                className={`sortLabel ${!isLatest ? 'active' : ''}`}
                            >
                                인기
                            </span>
                        </div>

                        <div className="searchBar">
                            <TextInput
                                className="communitySearch"
                                name="search"
                                type="text"
                                placeholder="게시글 검색"
                                value={search}
                            />
                        </div>

                        <button
                            className="twiButton composeBtn"
                            onClick={() => router.push('/community/write')}
                            style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: 8,
                                background: 'transparent',
                                color: '#ffffff',
                                fontWeight: 600,
                                fontSize: 16,
                                padding: '8px 10px',
                                borderRadius: 8,
                            }}
                        >
                            <Image
                                src={addIcon}
                                alt="게시글 작성"
                                width={18}
                                height={18}
                            />
                            <span>게시글 작성</span>
                        </button>
                    </div>
                </div>
            </div>
            <div style={{ height: stickyHeight }} />

            <ClientInfiniteList
                {...props}
                sortBy={isLatest ? 'latest' : 'popular'}
                search={search}
            />
            {toastMsg ? (
                <Toast message={toastMsg} onDone={() => setToastMsg(null)} />
            ) : null}
        </section>
    );
}
