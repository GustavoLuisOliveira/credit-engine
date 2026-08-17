import React from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { SelectInput } from '../shared/form/inputs/SelectInput';
import { EmptyState } from '../shared/EmptyState.tsx';
import { RECEIVABLE_TYPE_OPTIONS } from '../../services/receivable/ReceivableType.ts';
import type { ReceivableType } from '../../services/receivable/ReceivableType.ts';
import type PricingParameterResponse from '../../services/pricing/dto/PricingParameterResponse.ts';
import DateUtils from '../../utils/DateUtils.ts';

interface Props {
    history: PricingParameterResponse[];
    loading: boolean;
    historyType: ReceivableType;
    onChangeType: (receivableType: ReceivableType) => void;
}

export const PricingParameterHistory: React.FC<Props> = ({ history, loading, historyType, onChangeType }) => {
    return (
        <Card title="Historico de Parametros">
            <div className="grid mb-3">
                <div className="col-12 md:col-4">
                    <SelectInput
                        label="Tipo de recebível"
                        id="historyType"
                        value={historyType}
                        options={RECEIVABLE_TYPE_OPTIONS}
                        showClear={false}
                        onChange={e => onChangeType(e.value)}
                    />
                </div>
            </div>

            {!loading && history?.length === 0 ? (
                <EmptyState message="Nenhum parâmetro cadastrado para este tipo." />
            ) : (
                <DataTable value={history} loading={loading} dataKey="id" stripedRows rowClassName={() => 'text-sm'}>
                    <Column header="Taxa base" body={(row: PricingParameterResponse) => `${row.baseRate}% a.m.`} />
                    <Column header="Spread" body={(row: PricingParameterResponse) => `${row.spreadRate}% a.m.`} />
                    <Column header="Vigente a partir de" body={(row: PricingParameterResponse) => DateUtils.formatDate(row.effectiveDate)} />
                    <Column header="Cadastrado em" body={(row: PricingParameterResponse) => DateUtils.formatDateTime(row.createdAt)} />
                </DataTable>
            )}
        </Card>
    );
};
