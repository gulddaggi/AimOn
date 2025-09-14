export default function LoadingIcon(props: {
    ref?: (el: HTMLDivElement | null) => void;
}) {
    return (
        <div ref={props.ref} className="loadingIcon">
            <div className="bounce1"></div>
            <div className="bounce2"></div>
            <div className="bounce3"></div>
        </div>
    );
}
