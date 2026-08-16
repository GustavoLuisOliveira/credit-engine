import { FloatLabel } from 'primereact/floatlabel';
import { Calendar, type CalendarProps } from 'primereact/calendar';
import React from "react";

interface Props extends CalendarProps {
    label: string;
    id: string;
    name?: string;
}

export const DateInput: React.FC<Props> = ({ label, id, name, value, onChange, ...rest }) => {
    return (
        <FloatLabel>
            <Calendar
                className="w-full"
                inputId={id}
                name={name ?? id}
                value={value}
                onChange={onChange}
                dateFormat="dd/mm/yy"
                appendTo={document.body}
                {...rest}
            />
            <label htmlFor={id}>{label}</label>
        </FloatLabel>
    );
};
