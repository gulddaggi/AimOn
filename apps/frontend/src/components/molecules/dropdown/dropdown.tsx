'use client';
import { Options } from '@/types/options';
import DropdownSelect from '@/components/molecules/dropdown/dropdown-select';
import DropdownOption from '@/components/molecules/dropdown/dropdown-options';
import { useState, useEffect, useRef } from 'react';

export default function Dropdown(props: {
    className: string;
    text: string;
    options: Options;
    func?: (value: number, name: string) => void;
}) {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    function dropdownClick() {
        setIsOpen(!isOpen);
    }

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (
                dropdownRef.current &&
                !dropdownRef.current.contains(event.target as Node)
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

    return (
        <div className="dropdown" ref={dropdownRef}>
            <DropdownSelect
                className={props.className}
                text={props.text}
                func={dropdownClick}
                isOpen={isOpen}
            />
            <div className={`dropdownOptions  ${isOpen}`}>
                {props.options.map((item, idx) => (
                    <DropdownOption
                        key={idx}
                        value={item.name}
                        name={item.name}
                        func={() => {
                            dropdownClick();
                            if (props.func != undefined)
                                props.func(item.value, item.name);
                        }}
                    />
                ))}
            </div>
        </div>
    );
}
