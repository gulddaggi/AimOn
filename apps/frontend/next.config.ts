import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
    output: 'standalone',
    images: {
        remotePatterns: [
            {
                protocol: 'https',
                hostname: 'aimon-media.s3.ap-northeast-2.amazonaws.com',
                pathname: '**',
            },
            {
                protocol: 'https',
                hostname: 'owcdn.net',
                pathname: '**',
            },
            {
                protocol: 'https',
                hostname: 'www.vlr.gg',
                pathname: '**',
            },
        ],
    },
};

export default nextConfig;
