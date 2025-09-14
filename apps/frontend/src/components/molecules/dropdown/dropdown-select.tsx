'use client';
import { useState, useEffect } from 'react';
import TwiButton from '@/components/atoms/buttons/twi-button';
import dropDown from '@/resources/dropdown/drop-down.svg';
import dropUp from '@/resources/dropdown/drop-up.svg';

export default function DropdownSelect(props: {
    className: string;
    text: string;
    isOpen: boolean;
    func: () => void;
}) {
    const imageArr = [dropDown, dropUp];
    const altArr = ['열기 버튼', '접기 버튼'];
    const [isDown, setIsDown] = useState(0);

    useEffect(() => {
        if (props.isOpen) setIsDown(1);
        else setIsDown(0);
    }, [props.isOpen]);

    function down() {
        setIsDown(1);
        props.func();
    }

    function up() {
        setIsDown(0);
        props.func();
    }

    const funcArr = [down, up];

    return (
        <>
            <TwiButton
                className={`dropdownSelect ${props.className}`}
                text={props.text}
                image={imageArr[isDown]}
                alt={altArr[isDown]}
                func={funcArr[isDown]}
            />
        </>
    );
}
