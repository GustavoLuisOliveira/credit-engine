import { FloatLabel } from 'primereact/floatlabel';
import { InputNumber, type InputNumberProps } from 'primereact/inputnumber';
import React from "react";

interface Props extends InputNumberProps {
    label: string;
    id: string;
    name?: string;
    currencyCode?: string;
}

export const MoneyInput: React.FC<Props> = ({
    label,
    id,
    name,
    currencyCode = 'BRL',
    value,
    onChange,
    ...rest
}) => {
    return (
        <FloatLabel>
            <InputNumber
                className="w-full"
                inputClassName="w-full"
                mode="currency"
                currency={currencyCode}
                locale="pt-BR"
                minFractionDigits={2}
                maxFractionDigits={4}
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
