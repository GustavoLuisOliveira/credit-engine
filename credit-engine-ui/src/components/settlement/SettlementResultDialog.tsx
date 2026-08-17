import React from 'react';
import { Dialog } from 'primereact/dialog';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Divider } from 'primereact/divider';
import type SettlementResponse from '../../services/settlement/dto/SettlementResponse.ts';
import type SettlementItemResponse from '../../services/settlement/dto/SettlementItemResponse.ts';
import MoneyUtils from '../../utils/MoneyUtils.ts';
import DateUtils from '../../utils/DateUtils.ts';

interface Props {
    settlement: SettlementResponse | null;
    onHide: () => void;
}

export const SettlementResultDialog: React.FC<Props> = ({ settlement, onHide }) => {
    if (!settlement) return null;

    return (
        <Dialog
            header="Liquidação Executada"
            visible={!!settlement}
            style={{ width: '75vw' }}
            onHide={onHide}
            modal
            breakpoints={{ '960px': '90vw', '641px': '100vw' }}
            maximizable
            appendTo={document.body}
        >
            <div className="grid mb-3">
                <div className="col-6 md:col-3">
                    <span className="block text-color-secondary text-sm">Data da Liquidação</span>
                    <span className="font-bold">{DateUtils.formatDateTime(settlement.settlementDateTime)}</span>
                </div>

                <div className="col-6 md:col-3">
                    <span className="block text-color-secondary text-sm">Valor Total de Face</span>
                    <span className="font-bold">
                        {MoneyUtils.formatar(settlement.totalFaceValue, settlement.targetCurrencyCode)}
                    </span>
                </div>

                <div className="col-6 md:col-3">
                    <span className="block text-color-secondary text-sm">Deságio Total</span>
                    <span className="font-bold text-red-400">
                        - {MoneyUtils.formatar(settlement.totalDiscountAmount, settlement.targetCurrencyCode)}
                    </span>
                </div>

                <div className="col-12 md:col-3">
                    <Divider className="md:hidden" />
                    <span className="block text-color-secondary text-sm">Valor Líquido Total</span>
                    <span className="font-bold text-green-400 text-xl">
                        {MoneyUtils.formatar(settlement.totalNetAmount, settlement.targetCurrencyCode)}
                    </span>
                </div>
            </div>

            <Divider />

            <DataTable
                value={settlement.items}
                dataKey="id"
                stripedRows
                rowClassName={() => 'text-sm'}
                emptyMessage="Nenhum item nesta liquidação."
            >
                <Column
                    header="Recebível"
                    body={(row: SettlementItemResponse) => row.receivableId.substring(0, 8)}
                />
                <Column
                    header="Valor de Face"
                    body={(row: SettlementItemResponse) => MoneyUtils.formatar(row.faceValue, row.originalCurrencyCode)}
                />
                <Column
                    header="Prazo"
                    body={(row: SettlementItemResponse) => `${row.termMonths} mes(es)`}
                />
                <Column
                    header="Taxa Total"
                    body={(row: SettlementItemResponse) => MoneyUtils.formatarPercentual(row.totalRate)}
                />
                <Column
                    header="Deságio"
                    body={(row: SettlementItemResponse) => MoneyUtils.formatar(row.discountAmount, row.originalCurrencyCode)}
                />
                <Column
                    header="Valor Presente"
                    body={(row: SettlementItemResponse) => MoneyUtils.formatar(row.presentValue, row.originalCurrencyCode)}
                />
                <Column
                    header="Câmbio"
                    body={(row: SettlementItemResponse) => row.exchangeRateUsed}
                />
                <Column
                    header="Valor Liquidado"
                    body={(row: SettlementItemResponse) => MoneyUtils.formatar(row.settlementAmount, row.settlementCurrencyCode)}
                />
            </DataTable>
        </Dialog>
    );
};
