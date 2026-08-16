import React, { useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import type CurrencyResponse from '../../services/currency/dto/CurrencyResponse';
import type CurrencyRequest from '../../services/currency/dto/CurrencyRequest';
import DateUtils from "../../utils/DateUtils.ts";
import {EmptyState} from "../shared/EmptyState.tsx";
import {Loading} from "../shared/Loading.tsx";
import {AddButton} from "../shared/btns/AddButton.tsx";
import {FormCurrency} from "./FormCurrency.tsx";

interface Props {
    currencies: CurrencyResponse[];
    loading: boolean;
    creating: boolean;
    create: (request: CurrencyRequest) => Promise<CurrencyResponse | null>;
}

export const Currencies: React.FC<Props> = ({ currencies, loading, creating, create }) => {
    const [formVisible, setFormVisible] = useState(false);

    return (
        <>
            <Card
                className={'mb-3'}
                title={(
                    <div className="flex justify-content-between align-items-center">
                        <span>Moedas</span>
                        <AddButton label="Moeda" onClick={() => setFormVisible(true)} />
                    </div>
                )}
            >
                {loading ? (
                    <Loading />
                ) : currencies.length === 0 ? (
                    <EmptyState message="Nenhuma moeda cadastrada." />
                ) : (
                    <DataTable value={currencies} stripedRows dataKey="code" rowClassName={() => 'text-sm'}>
                        <Column field="code" header="Código" />
                        <Column field="name" header="Nome" />
                        <Column field="symbol" header="Simbolo" />
                        <Column header="Cadastrada em" body={rowData => DateUtils.formatDateTime(rowData.createdAt)} />
                    </DataTable>
                )}
            </Card>

            <FormCurrency
                visible={formVisible}
                close={() => setFormVisible(false)}
                create={create}
                creating={creating}
            />
        </>
    );
};
