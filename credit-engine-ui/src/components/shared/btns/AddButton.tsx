import React from "react";
import { Button } from 'primereact/button';
import {Icon} from "../Icon.tsx";

interface Props {
    label?: string;
    onClick: () => void;
    className?: string;
    icon?: string;
}

export const AddButton: React.FC<Props> = ({ label = '', onClick, className = '', icon = 'docs_add_on' }) => {
    return (
        <Button
            label={label}
            icon={<Icon icon={icon} />}
            rounded
            outlined
            className={`${label ? 'gap-2' : ''} ${className}`}
            severity="success"
            tooltip={label ? `Adicionar ${label}` : undefined}
            tooltipOptions={{ position: 'top', showDelay: 200 }}
            onClick={onClick}
            type="button"
        />
    );
};
