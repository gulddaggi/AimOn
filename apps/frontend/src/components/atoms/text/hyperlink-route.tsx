import Link from 'next/link';

export default function HyperlinkRoute(props: {
    className: string;
    text: string;
    href: string;
}) {
    return (
        <Link className={`hyperlinkRoute ${props.className}`} href={props.href}>
            {props.text}
        </Link>
    );
}
