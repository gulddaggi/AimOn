export default function ToggleButton(props: {
    className: string;
    isToggled: boolean;
    func: () => void;
}) {
    return (
        <div className={`toggleButton ${props.className}`} onClick={props.func}>
            <div
                className={`toggleKnob ${props.className} ${props.isToggled}`}
            ></div>
        </div>
    );
}
