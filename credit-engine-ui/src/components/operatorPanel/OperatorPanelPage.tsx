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
import {useSettlements} from "../../hooks/settlement/useSettlements.ts";
import {SettlementActions} from "../settlement/SettlementActions.tsx";
import {SettlementResultDialog} from "../settlement/SettlementResultDialog.tsx";

export const OperatorPanel: React.FC = () => {
    const { currencies } = useCurrencies();
    const assignorSearch = useAssignors();
    const receivableHook = useReceivables();
    const princingSimulation = usePricingSimulation();
    const settlements = useSettlements();

    const [valuationDate, setValuationDate] = useState<Date | null>(new Date());
    const [targetCurrencyCode, setTargetCurrencyCode] = useState<string>('');

    const assignorId = assignorSearch.assignor?.id ?? null;

    const [selectedReceivableIds, setSelectedReceivableIds] = useState<string[]>([]);

    // Assim que o cedente e encontrado/cadastrado, carrega os recebíveis dele
    // para permitir a simulação (individual ou em lote) dos que ja estao em aberto.
    useEffect(() => {
        if (assignorId) receivableHook.findByAssignor(assignorId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [assignorId]);

    const handleSimulate = (receivableIds: string[]) => {
        princingSimulation.simulate(receivableIds, valuationDate, targetCurrencyCode);
    };

    const handleValuationDateChange = (date: Date | null) => {
        setValuationDate(date);

        if (selectedReceivableIds.length > 0) {
            princingSimulation.simulate(selectedReceivableIds, date, targetCurrencyCode);
        }
    };

    const handleTargetCurrencyCodeChange = (code: string) => {
        setTargetCurrencyCode(code);

        if (selectedReceivableIds.length > 0) {
            princingSimulation.simulate(selectedReceivableIds, valuationDate, code);
        }
    };

    // Apos liquidar, o cedente permanece selecionado. Recarrega os
    // recebíveis (os liquidados saem da lista de abertos) e descarta os
    // resultados de simulacao, ja que os recebíveis simulados foram liquidados.
    const handleSettled = () => {
        if (assignorId) receivableHook.findByAssignor(assignorId);
        princingSimulation.reset();
    };

    const handleNewOperation = () => {
        setValuationDate(new Date());
        setTargetCurrencyCode('');
        assignorSearch.reset();
        receivableHook.reset();
        princingSimulation.reset();
        setSelectedReceivableIds([]);
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
                    onSelectionChange={setSelectedReceivableIds}
                />
            )}

            <BatchPrincingSimulationResults simulating={princingSimulation.simulating} results={princingSimulation.results} />

            {assignorId && valuationDate && !princingSimulation.simulating && princingSimulation.results.length > 0 && (
                <SettlementActions
                    assignorId={assignorId}
                    valuationDate={valuationDate}
                    targetCurrencyCode={targetCurrencyCode}
                    results={princingSimulation.results}
                    executing={settlements.executing}
                    execute={settlements.execute}
                    onSettled={handleSettled}
                />
            )}

            <SettlementResultDialog settlement={settlements.settlement} onHide={settlements.closeResult} />
        </>
    );
};