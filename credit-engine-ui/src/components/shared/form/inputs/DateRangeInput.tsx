import React, { useState } from 'react';
import { FloatLabel } from 'primereact/floatlabel';
import { Calendar, type CalendarProps } from 'primereact/calendar';

interface Props extends CalendarProps<'range'> {
    label: string;
    id: string;
    name?: string;
}

export const DateRangeInput: React.FC<Props> = ({ label, id, name, value, onChange, ...rest }) => {
    const lastMonth = new Date();
    lastMonth.setMonth(lastMonth.getMonth() - 1);

    const [viewDate, setViewDate] = useState(lastMonth);

    return (
        <FloatLabel>
            <Calendar
                className="w-full"
                inputId={id}
                name={name ?? id}
                value={value}
                onChange={onChange}
                selectionMode="range"
                readOnlyInput
                dateFormat="dd/mm/yy"
                appendTo={document.body}
                showOtherMonths={false}
                numberOfMonths={2}
                yearNavigator
                // Controla qual mes aparece primeiro a esquerda
                viewDate={viewDate}
                onViewDateChange={e => setViewDate(e.value)}
                {...rest}
            />
            <label htmlFor={id}>{label}</label>
        </FloatLabel>
    );
};