'use client';
import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Toast from './toast';

export default function RouteToast() {
    const params = useSearchParams();
    const router = useRouter();
    const [message, setMessage] = useState<string | null>(null);

    useEffect(() => {
        const t = params.get('toast');
        if (t) setMessage(t);
    }, [params]);

    return message ? (
        <Toast
            message={message}
            onDone={() => {
                setMessage(null);
                const sp = new URLSearchParams(window.location.search);
                sp.delete('toast');
                const url = `${window.location.pathname}${sp.size ? `?${sp}` : ''}`;
                router.replace(url);
            }}
        />
    ) : null;
}
