import { Button } from 'primereact/button';
import {Icon} from "../Icon.tsx";

interface Props {
    label?: string;
    onClick: () => void;
    className?: string;
    disabled?: boolean;
    loading?: boolean;
}

export const SaveButton: React.FC<Props> = ({ label = 'Salvar', onClick, className, disabled = false, loading = false }) => {
    return (
        <Button
            label={label}
            icon={<Icon icon={'task_alt'} />}
            rounded
            className={`${label ? 'gap-2' : ''} ${className}`}
            severity="success"
            onClick={onClick}
            type="button"
            disabled={disabled}
            loading={loading}
        />
    );
};
