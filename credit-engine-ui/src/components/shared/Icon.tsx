import React, {type ReactNode} from 'react';

interface Props {
    icon: string;
    id?: string;
    size?: number;
    fill?: boolean;
    wght?: number;
    className?: string;
    children?: ReactNode;
}

export const Icon: React.FC<Props> = ({ icon, id, size, fill, wght = 400, className = "", children }) => {
    return (
        <span
            translate="no"
            className={`material-symbols-outlined notranslate ${className}`}
            id={id ?? undefined}
            style={{
                fontVariationSettings: `"FILL" ${fill ? '1' : '0'}, "wght" ${wght}, "GRAD" 0, "opsz" 48`,
                fontSize: `${size}px`
            }}
        >
            {icon}
            {children}
        </span>
    );
}