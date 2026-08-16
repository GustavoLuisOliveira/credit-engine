import React from 'react';
import { DateInput } from '../shared/form/inputs/DateInput.tsx';
import { SelectInput } from '../shared/form/inputs/SelectInput.tsx';
import type CurrencyResponse from '../../services/currency/dto/CurrencyResponse.ts';
import type { SelectOption } from '../shared/form/models/SelectOption.ts';

interface Props {
    currencies: CurrencyResponse[];
    valuationDate: Date | null;
    onValuationDateChange: (date: Date | null) => void;
    targetCurrencyCode: string;
    onTargetCurrencyCodeChange: (code: string) => void;
}

// Campos compartilhados entre a simulação do recebível novo e a simulação em
// lote dos recebíveis existentes. Ficam no topo, fora do form de recebível,
// para nao precisar reabrir o form so para trocar a moeda de liquidação.
export const PrincingSimulationParams: React.FC<Props> = ({
                                                      currencies,
                                                      valuationDate,
                                                      onValuationDateChange,
                                                      targetCurrencyCode,
                                                      onTargetCurrencyCodeChange,
                                                  }) => {
    const currencyOptions: SelectOption[] = currencies.map(c => ({ label: `${c.code} - ${c.name}`, value: c.code }));

    return (
        <div className="grid justify-content-center align-content-center mt-3">
            <div className="col-12 md:col-4 lg:col-3">
                <DateInput
                    label="Data de Referência da Simulação"
                    id="valuationDate"
                    value={valuationDate}
                    onChange={e => onValuationDateChange((e.value as Date) ?? null)}
                />
            </div>

            <div className="col-12 md:col-4">
                <SelectInput
                    label="Moeda de Liquidação"
                    id="targetCurrencyCode"
                    value={targetCurrencyCode}
                    options={currencyOptions}
                    onChange={e => onTargetCurrencyCodeChange(e.value)}
                />
            </div>
        </div>
    );
};
