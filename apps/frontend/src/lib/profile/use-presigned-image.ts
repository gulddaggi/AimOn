'use client';

import { useCallback, useEffect, useRef, useState } from 'react';

function isAbsoluteUrl(value: string) {
    return /^https?:\/\//i.test(value);
}

export function usePresignedImage(initialKeyOrUrl: string | null | undefined) {
    const [src, setSrc] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const lastRef = useRef<string | null>(null);
    const abortRef = useRef<AbortController | null>(null);

    const resolve = useCallback(
        async (keyOrUrl?: string | null | undefined) => {
            const candidate =
                keyOrUrl ?? lastRef.current ?? initialKeyOrUrl ?? null;
            lastRef.current = candidate;
            if (!candidate) {
                setSrc(null);
                return;
            }
            // 취소 처리
            if (abortRef.current) abortRef.current.abort();
            const controller = new AbortController();
            abortRef.current = controller;
            const normalized = candidate.replace(/^"|"$/g, '');
            try {
                setLoading(true);
                if (isAbsoluteUrl(normalized)) {
                    setSrc(normalized);
                    return;
                }
                const res = await fetch(
                    `/next-api/profile/s3-download-url?key=${encodeURIComponent(normalized)}`,
                    {
                        cache: 'no-store',
                        signal: controller.signal,
                    }
                );
                const data = await res.json();
                const url = typeof data === 'string' ? data : data.url;
                setSrc(url || null);
            } catch (e) {
                // 타입 안전한 AbortError 체크
                const isAbortError = (err: unknown): boolean => {
                    if (typeof err !== 'object' || err === null) return false;
                    const name = (err as { name?: unknown }).name;
                    return typeof name === 'string' && name === 'AbortError';
                };
                if (isAbortError(e)) return;
                setSrc(null);
            } finally {
                setLoading(false);
            }
        },
        [initialKeyOrUrl]
    );

    useEffect(() => {
        resolve(initialKeyOrUrl);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [initialKeyOrUrl]);

    return { src, loading, resolve };
}
