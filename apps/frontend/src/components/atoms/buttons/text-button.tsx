// 텍스트 버튼
// 함수 기반 상호작용 컴포넌트
'use client';

// 텍스트, 함수 필요
export default function TextButton(props: {
    className: string;
    text: string;
    func: () => void;
}) {
    return (
        <>
            <button
                className={`textButton ${props.className}`}
                onClick={props.func}
            >
                {props.text}
            </button>
        </>
    );
}
