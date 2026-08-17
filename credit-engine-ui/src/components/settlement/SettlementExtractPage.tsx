import React from 'react';
import { Card } from 'primereact/card';
import { DataTable, type DataTablePageEvent } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { useSettlementExtract } from '../../hooks/settlement/useSettlementExtract.ts';
import { useAssignorOptions } from '../../hooks/assignor/useAssignorOptions.ts';
import { useCurrencies } from '../../hooks/currency/useCurrencies.ts';
import { useSettlements } from '../../hooks/settlement/useSettlements.ts';
import { SettlementExtractFilters } from './SettlementExtractFilters.tsx';
import { SettlementResultDialog } from './SettlementResultDialog.tsx';
import { EmptyState } from '../shared/EmptyState.tsx';
import { Icon } from '../shared/Icon.tsx';
import type SettlementExtractResponse from '../../services/settlement/dto/SettlementExtractResponse.ts';
import MoneyUtils from '../../utils/MoneyUtils.ts';
import DateUtils from '../../utils/DateUtils.ts';

export const SettlementExtractPage: React.FC = () => {
    const extract = useSettlementExtract();
    const assignorOptions = useAssignorOptions();
    const { currencies } = useCurrencies();
    const settlements = useSettlements();

    const handlePageChange = (event: DataTablePageEvent) => {
        extract.changePage(event.page ?? 0);
    };

    return (
        <>
            <Card className="mb-3" title="Extrato de Liquidação">
                <SettlementExtractFilters
                    assignors={assignorOptions.assignors}
                    currencies={currencies}
                    loading={extract.loading}
                    onFilter={extract.applyFilter}
                />
            </Card>

            <Card>
                {extract.page?.content?.length === 0 && !extract.loading ? (
                    <EmptyState message="Nenhuma liquidação encontrada para os filtros informados." />
                ) : (
                    <DataTable
                        value={extract.page?.content}
                        loading={extract.loading}
                        dataKey="id"
                        stripedRows
                        rowClassName={() => 'text-sm'}
                        lazy
                        paginator
                        first={extract.page?.number * extract.pageSize}
                        rows={extract.pageSize}
                        totalRecords={extract.page?.totalElements}
                        onPage={handlePageChange}
                    >
                        <Column header="Cedente" body={(row: SettlementExtractResponse) => `${row.assignorName} - ${row.assignorDocumentNumber}`} />
                        <Column header="Data de Referência" body={(row: SettlementExtractResponse) => DateUtils.formatDate(row.valuationDate)} />
                        <Column header="Data da Liquidação" body={(row: SettlementExtractResponse) => DateUtils.formatDateTime(row.settlementDateTime)} />
                        <Column field="targetCurrencyCode" header="Moeda" />
                        <Column header="Valor de Face" body={(row: SettlementExtractResponse) => MoneyUtils.formatar(row.totalFaceValue, row.targetCurrencyCode)} />
                        <Column header="Deságio" body={(row: SettlementExtractResponse) => MoneyUtils.formatar(row.totalDiscountAmount, row.targetCurrencyCode)} />
                        <Column header="Valor Líquido" body={(row: SettlementExtractResponse) => MoneyUtils.formatar(row.totalNetAmount, row.targetCurrencyCode)} />
                        <Column
                            header="Itens"
                            body={(row: SettlementExtractResponse) => (
                                <Button
                                    icon={<Icon icon="info" />}
                                    rounded text outlined
                                    size="small"
                                    tooltip="Ver itens liquidados"
                                    tooltipOptions={{ position: 'top', showDelay: 200 }}
                                    onClick={() => settlements.findById(row.id)}
                                    loading={settlements.loading}
                                    type="button"
                                />
                            )}
                        />
                    </DataTable>
                )}
            </Card>

            <SettlementResultDialog settlement={settlements.settlement} onHide={settlements.closeResult} />
        </>
    );
};
