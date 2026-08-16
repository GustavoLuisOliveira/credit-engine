import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { Divider } from 'primereact/divider';
import { Icon } from '../shared/Icon.tsx';
import { BatchPrincingSimulationResults } from '../princing/BatchPrincingSimulationResults.tsx';
import { useCurrencies } from '../../hooks/currency/useCurrencies.ts';
import { useAssignors } from '../../hooks/assignor/useAssignors.ts';
import { useReceivables } from '../../hooks/receivable/useReceivables.ts';
import { AssignorSearch } from '../assignor/AssignorSearch.tsx';
import { ReceivableList } from '../receivable/ReceivableList.tsx';
import { PrincingSimulationParams } from '../princing/PrincingSimulationParams.tsx';
import {usePricingSimulation} from "../../hooks/princing/usePricingSimulation.ts";

export const OperatorPanel: React.FC = () => {
    const { currencies } = useCurrencies();
    const assignorSearch = useAssignors();
    const receivableHook = useReceivables();
    const princingSimulation = usePricingSimulation();

    const [valuationDate, setValuationDate] = useState<Date | null>(new Date());
    const [targetCurrencyCode, setTargetCurrencyCode] = useState<string>('');

    const assignorId = assignorSearch.assignor?.id ?? null;

    // Assim que o cedente e encontrado/cadastrado, carrega os recebíveis dele
    // para permitir a simulação (individual ou em lote) dos que ja estao em aberto.
    useEffect(() => {
        if (assignorId) receivableHook.findByAssignor(assignorId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [assignorId]);

    const handleSimulate = (receivableIds: string[]) => {
        princingSimulation.simulate(receivableIds, valuationDate, targetCurrencyCode);
    };

    // Data de referência e moeda de liquidação afetam o resultado de tudo que
    // ja foi simulado, entao qualquer alteração aqui refaz a simulação dos
    // recebíveis ja simulados (sem useEffect, disparado direto no handler).
    const handleValuationDateChange = (date: Date | null) => {
        setValuationDate(date);

        const simulatedIds = princingSimulation.results.map(r => r.receivableId);
        if (simulatedIds.length > 0) {
            princingSimulation.simulate(simulatedIds, date, targetCurrencyCode);
        }
    };

    const handleTargetCurrencyCodeChange = (code: string) => {
        setTargetCurrencyCode(code);

        const simulatedIds = princingSimulation.results.map(r => r.receivableId);
        if (simulatedIds.length > 0) {
            princingSimulation.simulate(simulatedIds, valuationDate, code);
        }
    };

    const handleNewOperation = () => {
        setValuationDate(new Date());
        setTargetCurrencyCode('');
        assignorSearch.reset();
        receivableHook.reset();
        princingSimulation.reset();
    };

    return (
        <>
            <Card
                className="mb-3"
                title={(
                    <div className="flex justify-content-between align-items-center">
                        <span>Painel do Operador</span>
                        <Button
                            label="Nova Operacao"
                            icon={<Icon icon="restart_alt"/>}
                            rounded
                            outlined
                            severity="secondary"
                            className="gap-2"
                            onClick={handleNewOperation}
                            type="button"
                            size={'small'}
                        />
                    </div>
                )}
            >
                <AssignorSearch
                    assignor={assignorSearch.assignor}
                    loading={assignorSearch.loading}
                    searched={assignorSearch.searched}
                    creating={assignorSearch.creating}
                    findByDocumentNumber={assignorSearch.findByDocumentNumber}
                    create={assignorSearch.create}
                    reset={assignorSearch.reset}
                />

                {assignorId && (
                    <div className={'flex flex-column'}>
                        <Divider />
                        <PrincingSimulationParams
                            currencies={currencies}
                            valuationDate={valuationDate}
                            onValuationDateChange={handleValuationDateChange}
                            targetCurrencyCode={targetCurrencyCode}
                            onTargetCurrencyCodeChange={handleTargetCurrencyCodeChange}
                        />
                    </div>
                )}
            </Card>

            {assignorId && (
                <ReceivableList
                    assignorId={assignorId}
                    receivables={receivableHook.receivables}
                    loading={receivableHook.loading}
                    currencies={currencies}
                    saving={receivableHook.saving}
                    save={receivableHook.save}
                    onReceivableSaved={() => receivableHook.findByAssignor(assignorId)}
                    onSimulate={handleSimulate}
                    onRemoveSimulate={princingSimulation.remove}
                />
            )}

            <BatchPrincingSimulationResults simulating={princingSimulation.simulating} results={princingSimulation.results} />
        </>
    );
};