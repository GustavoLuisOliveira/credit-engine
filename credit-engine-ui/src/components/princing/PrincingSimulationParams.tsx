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

// Campos compartilhados entre a simulacao do recebível novo e a simulacao em
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
        <div className="grid">
            <div className="col-12 md:col-6 mb-3">
                <DateInput
                    label="Data de Referencia da Simulacao"
                    id="valuationDate"
                    maxDate={new Date()}
                    value={valuationDate}
                    onChange={e => onValuationDateChange((e.value as Date) ?? null)}
                />
            </div>

            <div className="col-12 md:col-6 mb-3">
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
