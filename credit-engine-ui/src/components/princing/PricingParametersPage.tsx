import React, { useState } from 'react';
import { Card } from 'primereact/card';
import { AddButton } from '../shared/btns/AddButton.tsx';
import {usePricingParameters} from "../../hooks/princing/usePricingParameters.ts";
import {CurrentPricingParameters} from "./CurrentPricingParameters.tsx";
import {PricingParameterHistory} from "./PricingParameterHistory.tsx";
import {FormPricingParameter} from "./FormPricingParameter.tsx";

export const PricingParametersPage: React.FC = () => {
    const {
        current,
        loadingCurrent,
        history,
        loadingHistory,
        historyType,
        changeHistoryType,
        creating,
        create,
    } = usePricingParameters();

    const [formVisible, setFormVisible] = useState(false);

    return (
        <>
            <Card
                className="mb-3"
                title={(
                    <div className="flex justify-content-between align-items-center">
                        <span>Parâmetros de Precificação Vigentes</span>
                        <AddButton label="Parâmetro" onClick={() => setFormVisible(true)} />
                    </div>
                )}
            >
                <CurrentPricingParameters current={current} loading={loadingCurrent} />
            </Card>

            <PricingParameterHistory
                history={history}
                loading={loadingHistory}
                historyType={historyType}
                onChangeType={changeHistoryType}
            />

            <FormPricingParameter
                visible={formVisible}
                close={() => setFormVisible(false)}
                create={create}
                creating={creating}
            />
        </>
    );
};
