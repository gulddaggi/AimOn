'use client';

import { useEffect, useState } from 'react';

type Props = {
    message: string;
    durationMs?: number;
    onDone?: () => void;
};

export default function Toast({ message, durationMs = 2500, onDone }: Props) {
    const [phase, setPhase] = useState<'enter' | 'exit'>('enter');

    useEffect(() => {
        const exitTimer = setTimeout(() => setPhase('exit'), durationMs);
        const doneTimer = setTimeout(
            () => onDone && onDone(),
            durationMs + 300
        );
        return () => {
            clearTimeout(exitTimer);
            clearTimeout(doneTimer);
        };
    }, [durationMs, onDone]);

    return (
        <div className="toastContainer">
            <div className={`toastBody ${phase}`}>{message}</div>
        </div>
    );
}
