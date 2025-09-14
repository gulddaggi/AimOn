'use client';
import React, { useEffect, useState, useRef } from 'react';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import { textUpdate } from '@/lib/redux/feature/input/input-text-slice';
import {
    addMessage,
    resetMessages,
    fetchGuide,
} from '@/lib/redux/feature/chatbot/chat-slice';
import { chatbot } from '@/lib/redux/feature/chatbot/chat-slice';
import InputArea from '@/components/molecules/chatbot/input-area';
import MessagesArea from '@/components/molecules/chatbot/messages-area';
import TextButton from '@/components/atoms/buttons/text-button';
export default function Chatbot() {
    const [isOpen, setIsOpen] = useState(false);
    const dispatch = useAppDispatch();

    const data = useAppSelector(state => state.chat.messages);
    const isLoading = useAppSelector(state => state.chat.isLoading);
    const isSucceeded = useAppSelector(state => state.chat.isSucceeded);
    const text = useAppSelector(state => state.inputText.value);

    const chatbotRef: React.RefObject<HTMLDivElement | null> = useRef(null);
    const inputRef: React.RefObject<HTMLDivElement | null> = useRef(null);
    const inputTextRef: React.RefObject<HTMLInputElement | null> = useRef(null);

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (
                chatbotRef.current &&
                !chatbotRef.current.contains(event.target as Node)
            ) {
                setIsOpen(false);
            }
        }

        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isOpen]);

    useEffect(() => {
        const handleKeydown = (event: KeyboardEvent) => {
            if (event.key === 'Enter') chatbotAction(text);
        };

        inputRef.current?.addEventListener('keydown', handleKeydown);
        return () =>
            inputRef.current?.removeEventListener('keydown', handleKeydown); // eslint-disable-line react-hooks/exhaustive-deps
    }, [text]); // eslint-disable-line react-hooks/exhaustive-deps

    useEffect(() => {
        dispatch(textUpdate(''));
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    useEffect(() => {
        if (!isOpen) {
            dispatch(resetMessages());
        }
    }, [isOpen, dispatch]);

    useEffect(() => {
        if (isSucceeded === 'success') {
            if (inputTextRef.current) {
                inputTextRef.current.value = '';
                inputTextRef.current.focus();
            }
        }
    }, [isSucceeded]);

    const chatbotAction = async (question: string) => {
        if (text === '') return;

        if (!isOpen) setIsOpen(true);

        dispatch(
            addMessage({
                context: question,
                sender: 'user',
            })
        );

        dispatch(chatbot(question));
    };

    return (
        <div className="chatbot" ref={chatbotRef}>
            <InputArea
                inputRef={inputRef}
                inputTextRef={inputTextRef}
                func={() => {
                    chatbotAction(text);
                }}
            />
            <div className="buttonArea">
                <TextButton
                    className="dict"
                    text="용어 사전"
                    func={() => {
                        chatbotAction(text);
                    }}
                />
                <TextButton
                    className="guide"
                    text="입문 가이드"
                    func={() => {
                        if (!isOpen) setIsOpen(true);
                        dispatch(addMessage({
                            context: '입문 가이드',
                            sender: 'user',
                        }));
                        dispatch(fetchGuide('init'));
                    }}
                />
            </div>
            {isOpen ? <MessagesArea data={data} isLoading={isLoading} /> : null}
        </div>
    );
}
