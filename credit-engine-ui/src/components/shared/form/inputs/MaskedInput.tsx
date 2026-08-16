import { FloatLabel } from 'primereact/floatlabel';
import { InputMask, type InputMaskProps } from 'primereact/inputmask';
import React from "react";

interface Props extends InputMaskProps {
    label: string;
    id: string;
    name?: string;
}

export const MaskedInput: React.FC<Props> = ({ label, id, name, mask, value, onChange, ...rest }) => {
    return (
        <FloatLabel>
            <InputMask
                mask={mask}
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
