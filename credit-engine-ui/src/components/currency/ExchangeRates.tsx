import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Message } from 'primereact/message';
import { SelectInput } from '../shared/form/inputs/SelectInput';
import { AddButton } from '../shared/btns/AddButton';
import type CurrencyResponse from '../../services/currency/dto/CurrencyResponse';
import DateUtils from '../../utils/DateUtils';
import {useExchangeRates} from "../../hooks/currency/useExchangeRates.ts";
import {FormExchangeRate} from "./FormExchangeRate.tsx";
import type {SelectOption} from "../shared/form/models/SelectOption.ts";

interface Props {
    currencies: CurrencyResponse[];
}

export const ExchangeRates: React.FC<Props> = ({ currencies }) => {
    const { latestRate, loading, searched, findLatestRate, create, creating, reset } = useExchangeRates();
    const [formVisible, setFormVisible] = useState(false);

    const [origin, setOrigin] = useState<string>('');
    const [destination, setDestination] = useState<string>('');

    const currencyOptions: SelectOption[] = currencies?.map(c => ({ label: `${c.code} - ${c.name}`, value: c.code })) ?? [];
    const destinationOptions: SelectOption[] = currencyOptions?.filter(option => option.value !== origin) ?? [];

    const handleOriginChange = (value: string) => {
        setOrigin(value);
        if (destination === value) setDestination('');
    };

    useEffect(() => {
        if (origin && destination) {
            findLatestRate(origin, destination);
        } else {
            reset();
        }
    }, [origin, destination, findLatestRate, reset]);

    return (
        <>
            <Card
                className={'mb-3'}
                title={(
                    <div className="flex justify-content-between align-items-center">
                        <span>Cotações de Câmbio</span>
                        <AddButton label="Cotação" onClick={() => setFormVisible(true)} />
                    </div>
                )}
                pt={{body: {className: 'px-4 py-3'}, content: {className: 'p-0'},}}
            >
                <p className="text-color-secondary mb-5">
                    Consultar a cotação mais recente entre duas moedas
                </p>

                <div className="grid align-items-end justify-content-center">
                    <div className="col-12 md:col-4">
                        <SelectInput
                            label="Origem"
                            id="origin"
                            value={origin}
                            options={currencyOptions}
                            onChange={e => handleOriginChange(e.value)}
                        />
                    </div>

                    <div className="col-12 md:col-4">
                        <SelectInput
                            label="Destino"
                            id="destination"
                            value={destination}
                            options={destinationOptions}
                            onChange={e => setDestination(e.value)}
                            disabled={!origin}
                        />
                    </div>
                </div>

                {loading && (
                    <Message
                        severity="info"
                        className="w-full justify-content-start"
                        text="Buscando cotação..."
                    />
                )}

                {searched && !loading && (
                    latestRate ? (
                        <Message
                            severity="success"
                            className="w-full justify-content-start"
                            text={`1 ${latestRate.originCurrencyCode} = ${latestRate.rate} ${latestRate.destinationCurrencyCode} (cotado em ${DateUtils.formatDateTime(latestRate.rateDateTime)})`}
                        />
                    ) : (
                        <Message
                            severity="warn"
                            className="w-full justify-content-start"
                            text="Nenhuma cotação encontrada para o par de moedas informado."
                        />
                    )
                )}
            </Card>

            <FormExchangeRate
                visible={formVisible}
                close={() => setFormVisible(false)}
                currencies={currencies}
                create={create}
                creating={creating}
            />
        </>
    );
};