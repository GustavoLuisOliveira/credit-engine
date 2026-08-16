import { Button } from 'primereact/button';
import React from "react";
import {Icon} from "../Icon.tsx";

interface Props {
    label?: string;
    onClick: () => void;
}

export const CancelButton: React.FC<Props> = ({ label = 'Cancelar', onClick }) => {
    return (
        <Button
            label={label}
            icon={<Icon icon={'close'} />}
            rounded
            outlined
            className="gap-2"
            severity="secondary"
            onClick={onClick}
            type="button"
        />
    );
};
