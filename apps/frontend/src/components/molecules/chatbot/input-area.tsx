'use client';
import { ImageElement } from '@/types/image';
import TextInput from '@/components/atoms/inputs/text-input';
import LocalImage from '@/components/atoms/contents/local-image';
import IconButton from '@/components/atoms/buttons/icon-button';
import aMon from '@/resources/chatbot/a-mon.svg';
import searchIcon from '@/resources/chatbot/search-icon.svg';
import React from 'react';

const resizedSearchIcon: ImageElement = {
    src: searchIcon.src,
    width: 30,
    height: 30,
};

export default function InputArea(props: {
    inputRef: React.RefObject<HTMLDivElement | null>;
    inputTextRef: React.RefObject<HTMLInputElement | null>;
    func: () => void;
}) {
    return (
        <div className="inputArea" ref={props.inputRef}>
            <LocalImage
                className="aMon"
                image={aMon}
                alt="에이몬"
                width={45}
                height={45}
            />
            <TextInput
                className="chatbotInput"
                name="chatbot"
                type="text"
                placeholder="에이몬에게 궁금한 fps e스포츠 정보를 물어보세요!"
                outerRef={props.inputTextRef}
            />
            <IconButton
                className="search"
                image={resizedSearchIcon}
                alt="검색 아이콘"
                func={props.func}
            />
        </div>
    );
}
