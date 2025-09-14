// 텍스트 입력 필드
'use client';

import React, { useRef } from 'react';
import { useAppDispatch } from '@/lib/redux/hooks';
import { textUpdate } from '@/lib/redux/feature/input/input-text-slice';
import { emailUpdate } from '@/lib/redux/feature/input/input-email-slice';
import { passwordUpdate } from '@/lib/redux/feature/input/input-password-slice';

// 필드명, 필드 타입 필요
export default function TextInput(props: {
    className: string;
    name: string;
    type: string;
    placeholder?: string;
    value?: string;
    outerRef?: React.RefObject<HTMLInputElement | null>;
    onChange?: (value: string) => void;
}) {
    // 내부에서 dom 객체 택스트 접근
    const innerRef: React.RefObject<HTMLInputElement | null> = useRef(null);
    const dispatch = useAppDispatch();

    let onChangeFunc = () => {
        if (props.onChange) {
            props.onChange(innerRef.current?.value as string);
            return;
        }
        dispatch(textUpdate(innerRef.current?.value as string));
    };

    switch (props.name) {
        case 'email': {
            onChangeFunc = () => {
                if (props.onChange) {
                    props.onChange(innerRef.current?.value as string);
                    return;
                }
                dispatch(emailUpdate(innerRef.current?.value as string));
            };
            break;
        }
        case 'password': {
            onChangeFunc = () => {
                if (props.onChange) {
                    props.onChange(innerRef.current?.value as string);
                    return;
                }
                dispatch(passwordUpdate(innerRef.current?.value as string));
            };
            break;
        }
        case 'chatbot': {
            onChangeFunc = () => {
                if (props.onChange) {
                    props.onChange(props.outerRef?.current?.value as string);
                    return;
                }
                dispatch(textUpdate(props.outerRef?.current?.value as string));
            };
        }
    }

    return (
        <>
            <input
                className={`textInput ${props.className}`}
                name={props.name}
                type={props.type}
                placeholder={props.placeholder}
                ref={props.outerRef || innerRef}
                value={props.value}
                onChange={onChangeFunc}
            ></input>
        </>
    );
}
