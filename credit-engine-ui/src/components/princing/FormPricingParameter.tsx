import React, { useEffect, useState } from 'react';
import { FormDialogContainer } from '../shared/form/FormDialogContainer';
import { SelectInput } from '../shared/form/inputs/SelectInput';
import { NumberInput } from '../shared/form/inputs/NumberInput';
import { DateInput } from '../shared/form/inputs/DateInput';
import type PricingParameterRequest from '../../services/pricing/dto/PricingParameterRequest';
import type PricingParameterResponse from '../../services/pricing/dto/PricingParameterResponse';
import { RECEIVABLE_TYPE_OPTIONS } from '../../services/receivable/ReceivableType.ts';

interface Props {
    visible: boolean;
    close: () => void;
    create: (request: PricingParameterRequest) => Promise<PricingParameterResponse | null>;
    creating: boolean;
}

const EMPTY_REQUEST: PricingParameterRequest = {
    receivableType: '',
    baseRate: null,
    spreadRate: null,
    effectiveDate: null,
};

export const FormPricingParameter: React.FC<Props> = ({ visible, close, create, creating }) => {
    const [request, setRequest] = useState<PricingParameterRequest>(EMPTY_REQUEST);

    useEffect(() => {
        if (!visible) return;
        setRequest({ ...EMPTY_REQUEST, effectiveDate: new Date() });
    }, [visible]);

    const handleSave = async () => {
        const response = await create(request);
        if (response) close();
    };

    return (
        <FormDialogContainer
            visible={visible}
            close={close}
            save={handleSave}
            title="Novo Parâmetro de Precificação"
            saving={creating}
        >
            <div className="grid">
                <div className="col-12 md:col-6 mb-3">
                    <SelectInput
                        label="Tipo de recebível"
                        id="receivableType"
                        value={request.receivableType}
                        options={RECEIVABLE_TYPE_OPTIONS}
                        onChange={e => setRequest({ ...request, receivableType: e.value })}
                    />
                </div>

                <div className="col-12 md:col-6 mb-3">
                    <DateInput
                        label="Vigente a partir de"
                        id="effectiveDate"
                        value={request.effectiveDate}
                        onChange={e => setRequest({ ...request, effectiveDate: (e.value as Date) ?? null })}
                    />
                </div>

                <div className="col-12 md:col-6 mb-3">
                    <NumberInput
                        label="Taxa base (% a.m.)"
                        id="baseRate"
                        value={request.baseRate}
                        min={0}
                        max={100}
                        minFractionDigits={2}
                        maxFractionDigits={4}
                        onChange={e => setRequest({ ...request, baseRate: e.value ?? null })}
                    />
                </div>

                <div className="col-12 md:col-6 mb-3">
                    <NumberInput
                        label="Spread (% a.m.)"
                        id="spreadRate"
                        value={request.spreadRate}
                        min={0}
                        max={100}
                        minFractionDigits={2}
                        maxFractionDigits={4}
                        onChange={e => setRequest({ ...request, spreadRate: e.value ?? null })}
                    />
                </div>
            </div>
        </FormDialogContainer>
    );
};
