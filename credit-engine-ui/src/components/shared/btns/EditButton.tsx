import { Button } from 'primereact/button';
import {Icon} from "../Icon.tsx";
import React from "react";

interface Props {
    onClick: () => void;
    label?: string;
    icon?: string;
}

export const EditButton: React.FC<Props> = ({ onClick, label, icon = 'edit_note' }) => {
    return (
        <Button
            icon={<Icon icon={icon} />}
            label={label}
            className={label ? 'gap-2' : ''}
            rounded
            outlined
            size={'small'}
            tooltip="Editar"
            tooltipOptions={{ position: 'top', showDelay: 200 }}
            onClick={onClick}
            type="button"
        />
    );
};
