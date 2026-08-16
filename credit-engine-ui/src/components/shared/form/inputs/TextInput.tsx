import { FloatLabel } from 'primereact/floatlabel';
import { InputText, type InputTextProps } from 'primereact/inputtext';
import React from "react";

interface Props extends InputTextProps {
    label: string;
    id: string;
    name?: string;
}

export const TextInput: React.FC<Props> = ({ label, id, name, value, onChange, ...rest }) => {
    return (
        <FloatLabel>
            <InputText
                autoComplete="off"
                className="w-full"
                id={id}
                name={name ?? id}
                value={value}
                onChange={onChange}
                {...rest}
            />
            <label htmlFor={id}>{label}</label>
        </FloatLabel>
    );
};
