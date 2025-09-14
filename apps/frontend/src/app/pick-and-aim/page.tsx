import PickAndAimTemplate from '@/components/templates/pick-and-aim-template';
import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';

export default async function Page() {
    const cookieStore = await cookies();
    const accessToken = cookieStore.get('accessToken');
    if (!accessToken) {
        redirect('/login?next=/pick-and-aim');
    }

    return (
        <div className="page">
            <PickAndAimTemplate />
        </div>
    );
}
