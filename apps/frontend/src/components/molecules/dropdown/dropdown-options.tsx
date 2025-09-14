'use client';
import TextButton from '@/components/atoms/buttons/text-button';

export default function DropdownOption(props: {
    key: number;
    value: string;
    name: string;
    func?: () => void;
}) {
    return (
        <>
            <TextButton
                className={props.value}
                text={props.name}
                func={props.func || (() => {})}
            />
        </>
    );
}
