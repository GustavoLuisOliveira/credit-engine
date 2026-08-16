import React, { useEffect, useState } from 'react';
import { FormDialogContainer } from '../shared/form/FormDialogContainer.tsx';
import { TextInput } from '../shared/form/inputs/TextInput.tsx';
import { MaskedInput } from '../shared/form/inputs/MaskedInput.tsx';
import type AssignorRequest from '../../services/assignor/dto/AssignorRequest.ts';
import type AssignorResponse from '../../services/assignor/dto/AssignorResponse.ts';

interface Props {
    visible: boolean;
    close: () => void;
    prefillDocumentNumber: string;
    create: (request: AssignorRequest) => Promise<AssignorResponse | null>;
    creating: boolean;
}

const EMPTY_ASSIGNOR: AssignorRequest = { documentNumber: '', name: '', email: '', phone: '' };

export const FormAssignor: React.FC<Props> = ({ visible, close, prefillDocumentNumber, create, creating }) => {
    const [assignor, setAssignor] = useState<AssignorRequest>(EMPTY_ASSIGNOR);

    useEffect(() => {
        if (!visible) return;
        setAssignor({ ...EMPTY_ASSIGNOR, documentNumber: prefillDocumentNumber });
    }, [visible, prefillDocumentNumber]);

    const handleSave = async () => {
        const response = await create(assignor);
        if (response) close();
    };

    return (
        <FormDialogContainer
            visible={visible}
            close={close}
            save={handleSave}
            title="Cadastrar Cedente"
            saving={creating}
        >
            <div className="grid">
                <div className="col-12 md:col-3 mb-3">
                    <MaskedInput
                        label="CNPJ"
                        id="assignorDocumentNumber"
                        mask="99.999.999/9999-99"
                        value={assignor.documentNumber}
                        onChange={e => setAssignor({ ...assignor, documentNumber: e.value ?? '' })}
                    />
                </div>

                <div className="col-12 md:col-4 mb-3">
                    <TextInput
                        label="Razao Social"
                        id="assignorName"
                        value={assignor.name}
                        onChange={e => setAssignor({ ...assignor, name: e.target.value })}
                    />
                </div>

                <div className="col-12 md:col-3 mb-3">
                    <TextInput
                        label="E-mail"
                        id="assignorEmail"
                        value={assignor.email}
                        onChange={e => setAssignor({ ...assignor, email: e.target.value })}
                    />
                </div>

                <div className="col-12 md:col-2 mb-3">
                    <TextInput
                        label="Telefone"
                        id="assignorPhone"
                        value={assignor.phone}
                        onChange={e => setAssignor({ ...assignor, phone: e.target.value })}
                    />
                </div>
            </div>
        </FormDialogContainer>
    );
};
