import { Suspense } from 'react';
import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import SelectScreen from './select-screen';

export default async function Page() {
    const cookieStore = await cookies();
    const accessToken = cookieStore.get('accessToken');
    if (!accessToken) {
        redirect('/login?next=/pick-and-aim/select');
    }
    return (
        <Suspense>
            <SelectScreen />
        </Suspense>
    );
}
