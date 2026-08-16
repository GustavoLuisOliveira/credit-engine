import { Dialog } from 'primereact/dialog';
import { Divider } from 'primereact/divider';
import React, { type ReactNode } from 'react';
import { Loading } from '../Loading';
import { CancelButton } from '../btns/CancelButton';
import { SaveButton } from '../btns/SaveButton';

interface Props {
    visible: boolean;
    close: () => void;
    save: () => void;
    saveButtonLabel?: string;
    title: string;
    children: ReactNode;
    loading?: boolean;
    saving?: boolean;
}

export const FormDialogContainer: React.FC<Props> = ({
    visible,
    close,
    save,
    saveButtonLabel,
    title,
    children,
    loading,
    saving,
}) => {
    return (
        <Dialog
            header={title}
            visible={visible}
            style={{ width: '75vw' }}
            onHide={close}
            modal
            breakpoints={{ '960px': '75vw', '641px': '100vw' }}
            maximizable
            appendTo={document.body}
        >
            {loading ? (
                <Loading />
            ) : (
                <>
                    <form className="mt-5" autoComplete="off">
                        {children}
                    </form>

                    <Divider align="right">
                        <CancelButton onClick={close} />
                        <SaveButton onClick={save} label={saveButtonLabel} loading={saving} className="ml-2" />
                    </Divider>
                </>
            )}
        </Dialog>
    );
};
