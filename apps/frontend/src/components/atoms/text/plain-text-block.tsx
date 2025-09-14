export default function PlainTextBlock(props: {
    className: string;
    text: string;
}) {
    return <p className={`plainTextBlock ${props.className}`}>{props.text}</p>;
}
