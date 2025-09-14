export default function Hyperlink(props: {
    className: string;
    text: string;
    href: string;
}) {
    return (
        <a className={`hyperlink ${props.className}`} href={props.href}>
            {props.text}
        </a>
    );
}
