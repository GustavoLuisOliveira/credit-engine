import React, { useState } from 'react';
import { Button } from 'primereact/button';
import { SelectInput } from '../shared/form/inputs/SelectInput.tsx';
import { Icon } from '../shared/Icon.tsx';
import type SettlementExtractFilter from '../../services/settlement/dto/SettlementExtractFilter.ts';
import { emptySettlementExtractFilter } from '../../services/settlement/dto/SettlementExtractFilter.ts';
import type AssignorResponse from '../../services/assignor/dto/AssignorResponse.ts';
import type CurrencyResponse from '../../services/currency/dto/CurrencyResponse.ts';
import type { SelectOption } from '../shared/form/models/SelectOption.ts';
import {DateRangeInput} from "../shared/form/inputs/DateRangeInput.tsx";

interface Props {
    assignors: AssignorResponse[];
    currencies: CurrencyResponse[];
    loading: boolean;
    onFilter: (filter: SettlementExtractFilter) => void;
}

export const SettlementExtractFilters: React.FC<Props> = ({ assignors, currencies, loading, onFilter }) => {
    const [filter, setFilter] = useState<SettlementExtractFilter>(emptySettlementExtractFilter);

    const assignorOptions: SelectOption[] = assignors.map(a => ({ label: `${a.name} - ${a.documentNumber}`, value: a.id }));
    const currencyOptions: SelectOption[] = currencies.map(c => ({ label: `${c.code} - ${c.name}`, value: c.code }));

    const handleClear = () => {
        setFilter(emptySettlementExtractFilter);
        onFilter(emptySettlementExtractFilter);
    };

    return (
        <div className="grid align-items-end">
            <div className="col-12 md:col-4 mb-3">
                <SelectInput
                    label="Cedente"
                    id="assignorId"
                    value={filter.assignorId}
                    options={assignorOptions}
                    onChange={e => setFilter({ ...filter, assignorId: e.value ?? '' })}
                />
            </div>

            <div className="col-12 md:col-3 mb-3">
                <SelectInput
                    label="Moeda de Liquidação"
                    id="currencyCode"
                    value={filter.currencyCode}
                    options={currencyOptions}
                    onChange={e => setFilter({ ...filter, currencyCode: e.value ?? '' })}
                />
            </div>

            <div className="col-12 md:col-3 mb-3">
                <DateRangeInput
                    label="Período"
                    id="valuationDate"
                    value={filter.valuationDateFrom ? [filter.valuationDateFrom, filter.valuationDateTo] : null}
                    onChange={e => {
                        const [from, to] = (e.value as Date[]) ?? [null, null];
                        setFilter({ ...filter, valuationDateFrom: from ?? null, valuationDateTo: to ?? null });
                    }}
                />
            </div>

            <div className="col-12 md:col-2 mb-3 flex gap-2 justify-content-end">
                <Button
                    icon={<Icon icon="close" />}
                    rounded
                    outlined
                    severity="secondary"
                    size="small"
                    onClick={handleClear}
                    type="button"
                    tooltip={`Limpar Filtros`}
                    tooltipOptions={{ position: 'top', showDelay: 200 }}
                />
                <Button
                    icon={<Icon icon="search"/>}
                    rounded
                    size="small"
                    onClick={() => onFilter(filter)}
                    loading={loading}
                    type="button"
                    tooltip={`Aplicar Filtros`}
                    tooltipOptions={{ position: 'top', showDelay: 200 }}
                />
            </div>
        </div>
    );
};
