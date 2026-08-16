import { FloatLabel } from 'primereact/floatlabel';
import { InputNumber, type InputNumberProps } from 'primereact/inputnumber';
import React from "react";

interface Props extends InputNumberProps {
    label: string;
    id: string;
    name?: string;
}

export const NumberInput: React.FC<Props> = ({ label, id, name, value, onChange, ...rest }) => {
    return (
        <FloatLabel>
            <InputNumber
                className="w-full"
                inputClassName="w-full"
                id={id}
                inputId={id}
                name={name ?? id}
                value={value}
                onChange={onChange}
                aria-autocomplete="none"
                {...rest}
            />
            <label htmlFor={id}>{label}</label>
        </FloatLabel>
    );
};
