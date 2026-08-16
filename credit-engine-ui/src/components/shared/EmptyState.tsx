import React from "react";

interface Props {
    message?: string;
}

export const EmptyState: React.FC<Props> = ({ message = 'Nenhum registro encontrado.' }) => {
    return (
        <div className="flex flex-column align-items-center justify-content-center p-5 text-color-secondary">
            <i className="pi pi-inbox text-4xl mb-3" />
            <span>{message}</span>
        </div>
    );
};
