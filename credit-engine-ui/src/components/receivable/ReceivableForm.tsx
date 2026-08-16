import React from 'react';
import { SelectInput } from '../shared/form/inputs/SelectInput.tsx';
import { TextInput } from '../shared/form/inputs/TextInput.tsx';
import { MoneyInput } from '../shared/form/inputs/MoneyInput.tsx';
import { DateInput } from '../shared/form/inputs/DateInput.tsx';
import { SaveButton } from '../shared/btns/SaveButton.tsx';
import { RECEIVABLE_TYPE_OPTIONS } from '../../services/receivable/ReceivableType.ts';
import type ReceivableRequest from '../../services/receivable/dto/ReceivableRequest.ts';
import type CurrencyResponse from '../../services/currency/dto/CurrencyResponse.ts';
import type { SelectOption } from '../shared/form/models/SelectOption.ts';

interface Props {
    receivable: ReceivableRequest;
    onChange: (receivable: ReceivableRequest) => void;
    currencies: CurrencyResponse[];
    onSimulate: () => void;
    simulating: boolean;
}

export const ReceivableForm: React.FC<Props> = ({ receivable, onChange, currencies, onSimulate, simulating }) => {
    const currencyOptions: SelectOption[] = currencies.map(c => ({ label: `${c.code} - ${c.name}`, value: c.code }));

    return (
        <div className="grid">
            <div className="col-12 md:col-3 mb-3">
                <SelectInput
                    label="Tipo de Recebível"
                    id="type"
                    value={receivable.type}
                    options={RECEIVABLE_TYPE_OPTIONS}
                    onChange={e => onChange({ ...receivable, type: e.value })}
                />
            </div>

            <div className="col-12 md:col-3 mb-3">
                <TextInput
                    label="Numero do Documento"
                    id="documentNumber"
                    maxLength={50}
                    value={receivable.documentNumber}
                    onChange={e => onChange({ ...receivable, documentNumber: e.target.value })}
                />
            </div>

            <div className="col-12 md:col-3 mb-3">
                <SelectInput
                    label="Moeda do Titulo"
                    id="currencyCode"
                    value={receivable.currencyCode}
                    options={currencyOptions}
                    onChange={e => onChange({ ...receivable, currencyCode: e.value })}
                />
            </div>

            <div className="col-12 md:col-3 mb-3">
                <MoneyInput
                    label="Valor de Face"
                    id="faceValue"
                    currencyCode={receivable.currencyCode || 'BRL'}
                    value={receivable.faceValue}
                    onChange={e => onChange({ ...receivable, faceValue: e.value ?? null })}
                />
            </div>

            <div className="col-12 md:col-4 mb-3">
                <DateInput
                    label="Data de Vencimento"
                    id="dueDate"
                    value={receivable.dueDate}
                    onChange={e => onChange({ ...receivable, dueDate: (e.value as Date) ?? null })}
                    minDate={new Date()}
                />
            </div>

            <div className="col-12 flex justify-content-end">
                <SaveButton label="Simular" onClick={onSimulate} loading={simulating} />
            </div>
        </div>
    );
};
