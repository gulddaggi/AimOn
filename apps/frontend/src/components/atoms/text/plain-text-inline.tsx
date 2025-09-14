export default function PlainTextInline(props: {
    className: string;
    text: string;
}) {
    return (
        <span className={`plainTextInline ${props.className}`}>
            {props.text}
        </span>
    );
}
