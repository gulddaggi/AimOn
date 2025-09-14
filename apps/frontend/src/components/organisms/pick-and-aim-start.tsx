'use client';
import TextButton from '@/components/atoms/buttons/text-button';
import { useRouter } from 'next/navigation';

export default function PickAndAimStart() {
    const router = useRouter();

    return (
        <div className="pickAimStart">
            <h1 className="title">Pick &amp; Aim</h1>
            <p className="subtitle">
                선호하는 키워드를 사격하고, 팀을 선택해 보세요!
            </p>

            <div className="actions">
                <TextButton
                    className="start primary"
                    text="키워드 사격"
                    func={() => router.push('/pick-and-aim/select?mode=aim')}
                />
                <TextButton
                    className="start secondary"
                    text="키워드 선택"
                    func={() => router.push('/pick-and-aim/select?mode=normal')}
                />
            </div>
        </div>
    );
}
