'use client';

import Image from 'next/image';
import editIcon from '@/resources/resource-edit-white.svg';
import profileFallback from '@/resources/community/resource-profile.png';
import changeIcon from '@/resources/resource-change-white.svg';
import Toast from '@/components/molecules/toast';
import { useEffect, useRef, useState } from 'react';
import type { StaticImageData } from 'next/image';
import { updateMyProfile } from '@/lib/axios/profile/profile-api';
import LoadingIcon from '@/components/atoms/contents/loading-icon';

type Props = {
    nickname: string;
    email: string;
    level: number;
    exp: number;
    expMax: number;
    profileImageUrl?: string | null;
    userId?: number | null;
    onEdit?: () => void;
};

export default function ProfileCard({
    nickname,
    email,
    level,
    exp,
    expMax,
    profileImageUrl,
    userId,
}: Props) {
    const percent = Math.min(
        100,
        Math.round((exp / Math.max(1, expMax)) * 100)
    );
    const [editing, setEditing] = useState(false);
    const [nameDraft, setNameDraft] = useState(nickname);
    const [toastMsg, setToastMsg] = useState<string | null>(null);
    const [profileSrc, setProfileSrc] = useState<
        string | StaticImageData | null
    >(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [uploading, setUploading] = useState<boolean>(false);
    const fileInputRef = useRef<HTMLInputElement | null>(null);
    const [pendingImageKey, setPendingImageKey] = useState<string | null>(null);
    const [pendingFile, setPendingFile] = useState<File | null>(null);
    const [objectUrl, setObjectUrl] = useState<string | null>(null);

    const onRegister = async () => {
        try {
            let keyToSave: string | null = pendingImageKey ?? null;

            // 보류 중인 파일이 있다면 지금 업로드 수행
            if (pendingFile) {
                if (!userId) throw new Error('missing userId');
                setUploading(true);
                const contentType = pendingFile.type || 'image/jpeg';
                const resUpload = await fetch(
                    `/next-api/profile/s3-upload-url?userId=${encodeURIComponent(String(userId))}&contentType=${encodeURIComponent(contentType)}`,
                    { cache: 'no-store' }
                );
                const { uploadUrl, key } = await resUpload.json();
                await fetch(uploadUrl, {
                    method: 'PUT',
                    headers: { 'Content-Type': contentType },
                    body: pendingFile,
                });
                keyToSave = key ?? null;
            }

            const payload = {
                nickname: nameDraft,
                profileImageUrl:
                    keyToSave ??
                    (typeof profileImageUrl === 'string'
                        ? profileImageUrl
                        : null),
            };
            const updated = await updateMyProfile(payload);
            if (updated) {
                // 응답 데이터로 갱신 및 이미지 다운로드 반영
                const newKey = updated.profileImageUrl ?? null;
                setNameDraft(updated.nickname ?? nameDraft);
                if (newKey) {
                    const normalized = String(newKey).replace(/^"|"$/g, '');
                    try {
                        const downRes = await fetch(
                            `/next-api/profile/s3-download-url?key=${encodeURIComponent(normalized)}`,
                            { cache: 'no-store' }
                        );
                        const { url } = await downRes.json();
                        if (typeof url === 'string' && url) setProfileSrc(url);
                    } catch {}
                }
                setToastMsg('프로필이 수정되었습니다.');
                setEditing(false);
                setPendingImageKey(null);
                setPendingFile(null);
                if (objectUrl) {
                    URL.revokeObjectURL(objectUrl);
                    setObjectUrl(null);
                }
            } else {
                setToastMsg('프로필 수정에 실패했습니다.');
            }
        } catch {
            setToastMsg('프로필 수정에 실패했습니다.');
        } finally {
            setUploading(false);
        }
    };

    // 초기 로드 시 프로필 이미지 url/key 처리
    useEffect(() => {
        let cancelled = false;
        const run = async () => {
            setLoading(true);
            try {
                if (!profileImageUrl) {
                    if (!cancelled) setProfileSrc(profileFallback);
                    return;
                }
                const isAbsolute = /^https?:\/\//i.test(profileImageUrl);
                if (isAbsolute) {
                    if (!cancelled) setProfileSrc(profileImageUrl);
                    return;
                }
                const normalizedKey = profileImageUrl.replace(/^"|"$/g, '');
                const res = await fetch(
                    `/next-api/profile/s3-download-url?key=${encodeURIComponent(normalizedKey)}`,
                    { cache: 'no-store' }
                );
                const { url } = await res.json();
                if (!cancelled)
                    setProfileSrc(
                        typeof url === 'string' && url ? url : profileFallback
                    );
            } catch {
                if (!cancelled) setProfileSrc(profileFallback);
            } finally {
                if (!cancelled) setLoading(false);
            }
        };
        run();
        return () => {
            cancelled = true;
        };
    }, [profileImageUrl]);

    return (
        <div className="profileCard">
            <div
                style={{
                    position: 'absolute',
                    top: 12,
                    right: 12,
                    display: 'flex',
                    gap: 8,
                    whiteSpace: 'nowrap',
                }}
            >
                {editing ? (
                    <>
                        <button
                            className="submitButton primaryActionButton"
                            onClick={onRegister}
                        >
                            등록
                        </button>
                        <button
                            className="submitButton dangerActionButton"
                            onClick={() => {
                                // 취소: 편집 상태/임시값 초기화
                                setEditing(false);
                                setNameDraft(nickname);
                                setPendingFile(null);
                                setPendingImageKey(null);
                                if (objectUrl) {
                                    URL.revokeObjectURL(objectUrl);
                                    setObjectUrl(null);
                                }
                                // 프로필 이미지 원복
                                if (
                                    typeof profileImageUrl === 'string' &&
                                    profileImageUrl
                                ) {
                                    const normalized = profileImageUrl.replace(
                                        /^"|"$/g,
                                        ''
                                    );
                                    fetch(
                                        `/next-api/profile/s3-download-url?key=${encodeURIComponent(normalized)}`,
                                        { cache: 'no-store' }
                                    )
                                        .then(r => r.json())
                                        .then(({ url }) => {
                                            if (typeof url === 'string' && url)
                                                setProfileSrc(url);
                                            else setProfileSrc(profileFallback);
                                        })
                                        .catch(() =>
                                            setProfileSrc(profileFallback)
                                        );
                                } else {
                                    setProfileSrc(profileFallback);
                                }
                            }}
                        >
                            취소
                        </button>
                    </>
                ) : (
                    <button
                        className="textButton profileEdit"
                        onClick={() => setEditing(true)}
                    >
                        <Image
                            src={editIcon}
                            alt="편집"
                            width={20}
                            height={20}
                        />{' '}
                        프로필 편집
                    </button>
                )}
            </div>
            <div className="profileRow">
                <div className="profileAvatarWrap">
                    <div
                        style={{
                            position: 'relative',
                            width: 140,
                            height: 140,
                        }}
                    >
                        {(loading || uploading) && (
                            <div
                                style={{
                                    position: 'absolute',
                                    inset: 0,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                }}
                            >
                                <LoadingIcon />
                            </div>
                        )}
                        <Image
                            src={profileSrc || profileFallback}
                            alt={`${nameDraft} profile`}
                            width={140}
                            height={140}
                            className="profileAvatarLarge"
                        />
                    </div>
                    {editing && (
                        <>
                            <button
                                className="iconButton profileImageEdit"
                                aria-label="프로필 이미지 수정"
                                onClick={() => fileInputRef.current?.click()}
                            >
                                <Image
                                    src={changeIcon}
                                    alt="프로필 이미지 수정"
                                    width={20}
                                    height={20}
                                />
                                <span className="tooltip">
                                    프로필 이미지 수정
                                </span>
                            </button>
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept="image/*"
                                style={{ display: 'none' }}
                                onChange={e => {
                                    const file = e.target.files?.[0];
                                    if (!file) return;
                                    // 업로드는 등록 시점에 수행. 지금은 로컬 미리보기만 설정
                                    if (objectUrl) {
                                        URL.revokeObjectURL(objectUrl);
                                        setObjectUrl(null);
                                    }
                                    const url = URL.createObjectURL(file);
                                    setObjectUrl(url);
                                    setProfileSrc(url);
                                    setPendingFile(file);
                                    setPendingImageKey(null);
                                }}
                            />
                        </>
                    )}
                </div>
                <div className="profileInfo">
                    <div className="profileMeta">
                        <div className="label">닉네임</div>
                        {editing ? (
                            <input
                                className="textInput nicknameInput"
                                style={{
                                    height: 36,
                                    padding: '0 10px',
                                    maxWidth: 260,
                                }}
                                value={nameDraft}
                                onChange={e => setNameDraft(e.target.value)}
                                placeholder="닉네임"
                            />
                        ) : (
                            <div className="value">{nameDraft}</div>
                        )}
                    </div>
                    <div className="profileMeta">
                        <div className="label">이메일</div>
                        <div className="value">{email}</div>
                    </div>
                    <div className="profileStat">
                        <div className="profileLevel">Lv.{level}</div>
                        <div className="profileExp">
                            Exp. {exp} / {expMax}
                        </div>
                    </div>
                    <div className="expBar">
                        <div
                            className="expFill"
                            style={{ width: `${percent}%` }}
                        />
                    </div>
                    {/* 편집 모드 시 상단 버튼이 등록으로 대체되어 하단 액션은 제거 */}
                </div>
            </div>
            {toastMsg ? (
                <Toast message={toastMsg} onDone={() => setToastMsg(null)} />
            ) : null}
        </div>
    );
}
