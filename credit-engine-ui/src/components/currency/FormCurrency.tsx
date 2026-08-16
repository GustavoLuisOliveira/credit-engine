import React, { useEffect, useState } from 'react';
import type CurrencyRequest from '../../services/currency/dto/CurrencyRequest';
import type CurrencyResponse from '../../services/currency/dto/CurrencyResponse';
import {FormDialogContainer} from "../shared/form/FormDialogContainer.tsx";
import {TextInput} from "../shared/form/inputs/TextInput.tsx";

interface Props {
    visible: boolean;
    close: () => void;
    create: (request: CurrencyRequest) => Promise<CurrencyResponse | null>;
    creating: boolean;
}

const EMPTY_CURRENCY: CurrencyRequest = { code: '', name: '', symbol: '' };

export const FormCurrency: React.FC<Props> = ({ visible, close, create, creating }) => {
    const [currency, setCurrency] = useState<CurrencyRequest>(EMPTY_CURRENCY);

    useEffect(() => {
        if (!visible) return;
        setCurrency(EMPTY_CURRENCY);
    }, [visible]);

    const handleSave = async () => {
        const response = await create(currency);
        if (response) close();
    };

    return (
        <FormDialogContainer
            visible={visible}
            close={close}
            save={handleSave}
            title="Adicionar Moeda"
            saving={creating}
        >
            <div className="grid">
                <div className="col-12 md:col-4 mb-3">
                    <TextInput
                        label="Codigo (ex: BRL)"
                        id="code"
                        maxLength={3}
                        value={currency.code}
                        onChange={e => setCurrency({ ...currency, code: e.target.value.toUpperCase() })}
                    />
                </div>

                <div className="col-12 md:col-4 mb-3">
                    <TextInput
                        label="Nome"
                        id="name"
                        value={currency.name}
                        onChange={e => setCurrency({ ...currency, name: e.target.value })}
                    />
                </div>

                <div className="col-12 md:col-4 mb-3">
                    <TextInput
                        label="Simbolo (ex: R$)"
                        id="symbol"
                        maxLength={5}
                        value={currency.symbol}
                        onChange={e => setCurrency({ ...currency, symbol: e.target.value })}
                    />
                </div>
            </div>
        </FormDialogContainer>
    );
};
