import { ProgressSpinner } from 'primereact/progressspinner';
import React from "react";

export const Loading: React.FC = () => {
    return (
        <div className="flex justify-content-center align-items-center p-5">
            <ProgressSpinner strokeWidth="4" />
        </div>
    );
};
