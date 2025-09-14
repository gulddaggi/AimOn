import SideBar from '@/components/organisms/side-bar';
import StoreProvider from './store-provider';
import '@/styles/main.scss';
import { Noto_Sans_KR } from 'next/font/google';

const notoSansKr = Noto_Sans_KR({
    subsets: ['latin'],
    weight: ['400', '500', '700'],
    display: 'swap',
});

export default function RootLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <html lang="en">
            <body className={notoSansKr.className}>
                <StoreProvider>
                    <SideBar />
                    {children}
                </StoreProvider>
            </body>
        </html>
    );
}
