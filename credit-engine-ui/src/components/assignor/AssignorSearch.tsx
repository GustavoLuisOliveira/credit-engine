import React, { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { MaskedInput } from '../shared/form/inputs/MaskedInput.tsx';
import { AddButton } from '../shared/btns/AddButton.tsx';
import { Icon } from '../shared/Icon.tsx';
import { FormAssignor } from './FormAssignor.tsx';
import type AssignorResponse from '../../services/assignor/dto/AssignorResponse.ts';
import type AssignorRequest from '../../services/assignor/dto/AssignorRequest.ts';

interface Props {
    assignor: AssignorResponse | null;
    loading: boolean;
    searched: boolean;
    creating: boolean;
    findByDocumentNumber: (documentNumber: string) => void;
    create: (request: AssignorRequest) => Promise<AssignorResponse | null>;
    reset: () => void;
    onAssignorSelected: (assignorId: string) => void;
}

export const AssignorSearch: React.FC<Props> = ({
                                                    assignor,
                                                    loading,
                                                    searched,
                                                    creating,
                                                    findByDocumentNumber,
                                                    create,
                                                    reset,
                                                    onAssignorSelected,
                                                }) => {
    const [documentNumber, setDocumentNumber] = useState('');
    const [formVisible, setFormVisible] = useState(false);

    // Propaga o id do cedente para o formulario do recebível sempre que ele
    // muda, seja por busca ou por cadastro inline.
    useEffect(() => {
        if (assignor) onAssignorSelected(assignor.id);
    }, [assignor, onAssignorSelected]);

    const handleChangeAssignor = () => {
        reset();
        setDocumentNumber('');
    };

    if (assignor) {
        return (
            <Message
                severity="success"
                className="w-full justify-content-start mb-3"
                content={(
                    <div className="flex align-items-center justify-content-between w-full">
                        <span><strong>{assignor.name}</strong> - {assignor.documentNumber}</span>
                        <Button label="Trocar" text onClick={handleChangeAssignor} type="button" />
                    </div>
                )}
            />
        );
    }

    return (
        <>
            <div className="grid align-items-center">
                <div className="col-12 md:col-4">
                    <MaskedInput
                        label="CNPJ do Cedente"
                        id="documentNumber"
                        mask="99.999.999/9999-99"
                        value={documentNumber}
                        onChange={e => setDocumentNumber(e.value ?? '')}
                    />
                </div>

                <div className="col-12 md:col-2">
                    <Button
                        label="Buscar"
                        icon={<Icon icon="search" className="mr-2" />}
                        rounded
                        outlined
                        className="gap-2"
                        onClick={() => findByDocumentNumber(documentNumber)}
                        loading={loading}
                        disabled={!documentNumber}
                        type="button"
                    />
                </div>
            </div>

            {searched && !loading && (
                <div className="flex align-items-center justify-content-between flex-wrap gap-3">
                    <Message
                        severity="warn"
                        className="justify-content-start"
                        text="Cedente nao encontrado para o CNPJ informado."
                    />
                    <AddButton label="Cedente" onClick={() => setFormVisible(true)} />
                </div>
            )}

            <FormAssignor
                visible={formVisible}
                close={() => setFormVisible(false)}
                prefillDocumentNumber={documentNumber}
                create={create}
                creating={creating}
            />
        </>
    );
};
