import React, { useEffect, useState } from 'react';
import { FormDialogContainer } from '../shared/form/FormDialogContainer';
import { SelectInput } from '../shared/form/inputs/SelectInput';
import { NumberInput } from '../shared/form/inputs/NumberInput';
import { DateInput } from '../shared/form/inputs/DateInput';
import type ExchangeRateRequest from '../../services/currency/dto/ExchangeRateRequest';
import type ExchangeRateResponse from '../../services/currency/dto/ExchangeRateResponse';
import type CurrencyResponse from '../../services/currency/dto/CurrencyResponse';
import type {SelectOption} from "../shared/form/models/SelectOption.ts";

interface Props {
    visible: boolean;
    close: () => void;
    currencies: CurrencyResponse[];
    create: (request: ExchangeRateRequest) => Promise<ExchangeRateResponse | null>;
    creating: boolean;
}

const EMPTY_REQUEST: ExchangeRateRequest = {
    originCurrencyCode: '',
    destinationCurrencyCode: '',
    rate: null,
    rateDateTime: null,
};

export const FormExchangeRate: React.FC<Props> = ({ visible, close, currencies, create, creating }) => {
    const [request, setRequest] = useState<ExchangeRateRequest>(EMPTY_REQUEST);

    useEffect(() => {
        if (!visible) return;
        setRequest({ ...EMPTY_REQUEST, rateDateTime: new Date() });
    }, [visible]);

    const currencyOptions: SelectOption[] = currencies?.map(c => ({ label: `${c.code} - ${c.name}`, value: c.code }));
    const destinationOptions: SelectOption[] = currencyOptions?.filter(option => option.value !== request.originCurrencyCode);

    const handleOriginChange = (value: string) => {
        setRequest(prev => ({
            ...prev,
            originCurrencyCode: value,
            destinationCurrencyCode: prev.destinationCurrencyCode === value ? '' : prev.destinationCurrencyCode,
        }));
    };

    const handleSave = async () => {
        const response = await create(request);
        if (response) close();
    };

    return (
        <FormDialogContainer
            visible={visible}
            close={close}
            save={handleSave}
            title="Nova Cotação"
            saving={creating}
        >
            <div className="grid">
                <div className="col-12 md:col-6 mb-3">
                    <SelectInput
                        label="Moeda de origem"
                        id="originCurrencyCode"
                        value={request.originCurrencyCode}
                        options={currencyOptions}
                        onChange={e => handleOriginChange(e.value)}
                    />
                </div>

                <div className="col-12 md:col-6 mb-3">
                    <SelectInput
                        label="Moeda de destino"
                        id="destinationCurrencyCode"
                        value={request.destinationCurrencyCode}
                        options={destinationOptions}
                        onChange={e => setRequest({ ...request, destinationCurrencyCode: e.value })}
                    />
                </div>

                <div className="col-12 md:col-6 mb-3">
                    <NumberInput
                        label="Taxa"
                        id="rate"
                        value={request.rate}
                        minFractionDigits={2}
                        maxFractionDigits={6}
                        onChange={e => setRequest({ ...request, rate: e.value ?? null })}
                    />
                </div>

                <div className="col-12 md:col-6 mb-3">
                    <DateInput
                        label="Data/hora da cotação"
                        id="rateDateTime"
                        showTime
                        hourFormat="24"
                        maxDate={new Date()}
                        value={request.rateDateTime}
                        onChange={e => setRequest({ ...request, rateDateTime: (e.value as Date) ?? null })}
                    />
                </div>
            </div>
        </FormDialogContainer>
    );
};