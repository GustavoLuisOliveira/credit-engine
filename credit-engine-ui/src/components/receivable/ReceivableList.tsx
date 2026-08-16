import React, { useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import type ReceivableResponse from '../../services/receivable/dto/ReceivableResponse.ts';
import { RECEIVABLE_TYPE_OPTIONS } from '../../services/receivable/ReceivableType.ts';
import { Loading } from '../shared/Loading.tsx';
import { EmptyState } from '../shared/EmptyState.tsx';
import { Icon } from '../shared/Icon.tsx';
import MoneyUtils from '../../utils/MoneyUtils.ts';
import DateUtils from '../../utils/DateUtils.ts';

interface Props {
    receivables: ReceivableResponse[];
    loading: boolean;
    simulating: boolean;
    onSimulateBatch: (receivableIds: string[]) => void;
}

const typeLabel = (type: string): string =>
    RECEIVABLE_TYPE_OPTIONS.find(option => option.value === type)?.label ?? type;

export const ReceivableList: React.FC<Props> = ({ receivables, loading, simulating, onSimulateBatch }) => {
    const [selected, setSelected] = useState<ReceivableResponse[]>([]);

    // A listagem traz todos os recebíveis do cedente. Aqui filtramos apenas
    // os em aberto, ja que liquidados/cancelados nao entram na simulacao.
    const openReceivables = receivables.filter(r => r.status === 'UNSETTLED');

    const handleSimulate = () => {
        onSimulateBatch(selected.map(r => r.id));
    };

    return (
        <Card
            className="mb-3"
            title={(
                <div className="flex justify-content-between align-items-center">
                    <span>Recebíveis em Aberto</span>
                    <Button
                        label="Simular em Lote"
                        icon={<Icon icon="task_alt" className="mr-2" />}
                        rounded
                        className="gap-2"
                        severity="success"
                        onClick={handleSimulate}
                        loading={simulating}
                        disabled={selected.length === 0}
                        type="button"
                    />
                </div>
            )}
        >
            {loading ? (
                <Loading />
            ) : openReceivables.length === 0 ? (
                <EmptyState message="Nenhum recebível em aberto para este cedente." />
            ) : (
                <DataTable
                    value={openReceivables}
                    selectionMode="checkbox"
                    selection={selected}
                    onSelectionChange={e => setSelected(e.value as ReceivableResponse[])}
                    dataKey="id"
                    stripedRows
                    rowClassName={() => 'text-sm'}
                >
                    <Column selectionMode="multiple" headerStyle={{ width: '3rem' }} />
                    <Column field="documentNumber" header="Documento" />
                    <Column header="Tipo" body={row => typeLabel(row.type)} />
                    <Column header="Valor de Face" body={row => MoneyUtils.formatar(row.faceValue, row.currencyCode)} />
                    <Column header="Vencimento" body={row => DateUtils.formatDate(row.dueDate)} />
                </DataTable>
            )}
        </Card>
    );
};