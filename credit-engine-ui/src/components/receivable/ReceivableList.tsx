import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Divider } from 'primereact/divider';
import type ReceivableResponse from '../../services/receivable/dto/ReceivableResponse.ts';
import type ReceivableRequest from '../../services/receivable/dto/ReceivableRequest.ts';
import type CurrencyResponse from '../../services/currency/dto/CurrencyResponse.ts';
import { RECEIVABLE_TYPE_OPTIONS } from '../../services/receivable/ReceivableType.ts';
import { Loading } from '../shared/Loading.tsx';
import { EmptyState } from '../shared/EmptyState.tsx';
import { Icon } from '../shared/Icon.tsx';
import { ReceivableForm } from './ReceivableForm.tsx';
import MoneyUtils from '../../utils/MoneyUtils.ts';
import DateUtils from '../../utils/DateUtils.ts';

interface Props {
    assignorId: string;
    receivables: ReceivableResponse[];
    loading: boolean;
    currencies: CurrencyResponse[];
    saving: boolean;
    save: (request: ReceivableRequest) => Promise<ReceivableResponse | null>;
    onReceivableSaved: () => void;
    onSimulate: (receivableIds: string[]) => void;
    onRemoveSimulate: (receivableIds: string[]) => void;
    onSelectionChange?: (receivableIds: string[]) => void;
}

const emptyReceivable = (assignorId: string): ReceivableRequest => ({
    assignorId,
    type: '',
    documentNumber: '',
    faceValue: null,
    currencyCode: '',
    dueDate: null,
});

const typeLabel = (type: string): string =>
    RECEIVABLE_TYPE_OPTIONS.find(option => option.value === type)?.label ?? type;

export const ReceivableList: React.FC<Props> = ({
                                                    assignorId,
                                                    receivables,
                                                    loading,
                                                    currencies,
                                                    saving,
                                                    save,
                                                    onReceivableSaved,
                                                    onSimulate,
                                                    onRemoveSimulate,
                                                    onSelectionChange
                                                }) => {
    const [selected, setSelected] = useState<ReceivableResponse[]>([]);
    const [formVisible, setFormVisible] = useState(false);
    const [receivable, setReceivable] = useState<ReceivableRequest>(emptyReceivable(assignorId));

    // A listagem traz todos os recebíveis do cedente. Aqui filtramos apenas
    // os em aberto, ja que liquidados/cancelados nao entram na simulacao.
    const openReceivables = receivables.filter(r => r.status === 'UNSETTLED');

    // Quando a lista e recarregada (ex: apos uma liquidacao), recebíveis
    // liquidados saem de openReceivables. Descarta a selecao desses itens
    // para nao manter ids fantasmas que nao existem mais na tabela.
    useEffect(() => {
        setSelected(prev => {
            const pruned = prev.filter(r => openReceivables.some(o => o.id === r.id));
            return pruned.length === prev.length ? prev : pruned;
        });
    }, [receivables]);

    useEffect(() => {
        onSelectionChange?.(selected.map(r => r.id));
    }, [onSelectionChange, selected]);

    // Simetrico ao marcar/desmarcar: um item novo na selecao e simulado na
    // hora, um item removido da selecao tem seu resultado descartado na hora.
    // Nenhum dos dois refaz a simulacao dos itens que ja estavam selecionados.
    const handleSelectionChange = (newSelected: ReceivableResponse[]) => {
        const previousIds = new Set(selected.map(r => r.id));
        const newSelectedIds = new Set(newSelected.map(r => r.id));

        const addedIds = newSelected
            .filter(r => !previousIds.has(r.id))
            .map(r => r.id);

        const removedIds = selected
            .filter(r => !newSelectedIds.has(r.id))
            .map(r => r.id);

        if (addedIds.length > 0) onSimulate(addedIds);
        if (removedIds.length > 0) onRemoveSimulate(removedIds);

        setSelected(newSelected);
    };

    const handleToggleForm = () => {
        if (!formVisible) setReceivable(emptyReceivable(assignorId));
        setFormVisible(v => !v);
    };

    const handleSaveAndSimulate = async () => {
        const saved = await save(receivable);
        if (!saved) return;

        onReceivableSaved();

        // Seleciona o recebível recem salvo junto com os que ja estavam
        // selecionados e simula apenas ele, sem refazer a simulacao dos demais.
        setSelected(prev => [...prev.filter(r => r.id !== saved.id), saved]);
        onSimulate([saved.id]);

        setReceivable(emptyReceivable(assignorId));
        setFormVisible(false);
    };

    return (
        <Card
            className="mb-3"
            title={(
                <div className="flex justify-content-between align-items-center flex-wrap gap-2">
                    <span>Recebíveis em Aberto</span>
                    <div className="flex gap-2">
                        <Button
                            label={formVisible ? 'Ocultar Formulário' : 'Novo Recebível'}
                            icon={<Icon icon={formVisible ? 'close' : 'add'} />}
                            rounded
                            outlined
                            className="gap-2"
                            onClick={handleToggleForm}
                            type="button"
                        />
                    </div>
                </div>
            )}
        >
            {formVisible && (
                <>
                    <ReceivableForm
                        receivable={receivable}
                        onChange={setReceivable}
                        currencies={currencies}
                        onSimulate={handleSaveAndSimulate}
                        simulating={saving}
                    />
                    <Divider />
                </>
            )}

            {loading ? (
                <Loading />
            ) : openReceivables.length === 0 ? (
                <EmptyState message="Nenhum recebível em aberto para este cedente." />
            ) : (
                <DataTable
                    value={openReceivables}
                    selectionMode="checkbox"
                    selection={selected}
                    onSelectionChange={e => handleSelectionChange(e.value as ReceivableResponse[])}
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