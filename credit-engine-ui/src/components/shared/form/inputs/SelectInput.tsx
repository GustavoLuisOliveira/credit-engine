import { FloatLabel } from 'primereact/floatlabel';
import { Dropdown, type DropdownProps } from 'primereact/dropdown';
import React from "react";

interface Props extends DropdownProps {
    label: string;
    id: string;
    name?: string;
}

export const SelectInput: React.FC<Props> = ({ label, id, value, options, onChange, ...rest }) => {
    return (
        <FloatLabel>
            <Dropdown
                showClear
                className="w-full"
                placeholder="Selecione..."
                emptyMessage="Nenhum resultado encontrado"
                emptyFilterMessage="Nenhum resultado encontrado"
                filter
                optionLabel="label"
                optionValue="value"
                id={id}
                value={value ?? ''}
                onChange={onChange}
                options={options}
                {...rest}
            />
            <label htmlFor={id}>{label}</label>
        </FloatLabel>
    );
};
