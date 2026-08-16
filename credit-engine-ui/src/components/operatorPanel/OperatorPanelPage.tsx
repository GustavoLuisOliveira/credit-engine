import React, { useCallback, useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { Divider } from 'primereact/divider';
import { Icon } from '../shared/Icon.tsx';
import type ReceivableRequest from '../../services/receivable/dto/ReceivableRequest.ts';
import {BatchPrincingSimulationResults} from "../princing/BatchPrincingSimulationResults.tsx";
import {PrincingSimulationResult} from "../princing/PrincingSimulationResult.tsx";
import {useCurrencies} from "../../hooks/currency/useCurrencies.ts";
import {useAssignors} from "../../hooks/assignor/useAssignors.ts";
import {useReceivables} from "../../hooks/receivable/useReceivables.ts";
import {usePricingSimulation} from "../../hooks/princing/usePricingSimulation.ts";
import {useBatchPricingSimulation} from "../../hooks/princing/useBatchPricingSimulation.ts";
import {AssignorSearch} from "../assignor/AssignorSearch.tsx";
import {ReceivableList} from "../receivable/ReceivableList.tsx";
import {ReceivableForm} from "../receivable/ReceivableForm.tsx";
import {PrincingSimulationParams} from "../princing/PrincingSimulationParams.tsx";

const EMPTY_RECEIVABLE: ReceivableRequest = {
    assignorId: '',
    type: '',
    documentNumber: '',
    faceValue: null,
    currencyCode: '',
    dueDate: null,
};

export const OperatorPanel: React.FC = () => {
    const { currencies } = useCurrencies();
    const assignorSearch = useAssignors();
    const receivableHook = useReceivables();
    const pricingSimulation = usePricingSimulation();
    const batchSimulation = useBatchPricingSimulation();

    const [receivable, setReceivable] = useState<ReceivableRequest>(EMPTY_RECEIVABLE);
    const [valuationDate, setValuationDate] = useState<Date | null>(new Date());
    const [targetCurrencyCode, setTargetCurrencyCode] = useState<string>('');
    const [newReceivableFormVisible, setNewReceivableFormVisible] = useState(false);

    const assignorId = assignorSearch.assignor?.id ?? null;

    // Assim que o cedente e encontrado/cadastrado, carrega os recebíveis dele
    // para permitir a simulacao em lote dos que ja estao em aberto.
    useEffect(() => {
        if (assignorId) receivableHook.findByAssignor(assignorId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [assignorId]);

    const handleAssignorSelected = useCallback((selectedAssignorId: string) => {
        setReceivable(prev => ({ ...prev, assignorId: selectedAssignorId }));
    }, []);

    const handleSimulate = async () => {
        const saved = await receivableHook.save(receivable);
        if (!saved) return;

        await pricingSimulation.simulate(saved.id, valuationDate, targetCurrencyCode);

        // Atualiza a lista de recebíveis em aberto para refletir o que
        // acabou de ser criado ou atualizado.
        if (assignorId) receivableHook.findByAssignor(assignorId);

        // Limpa o form e fecha a secao apos simular com sucesso.
        setReceivable({ ...EMPTY_RECEIVABLE, assignorId: assignorId ?? '' });
        setNewReceivableFormVisible(false);
    };

    const handleSimulateBatch = (receivableIds: string[]) => {
        batchSimulation.simulateBatch(receivableIds, valuationDate, targetCurrencyCode);
    };

    const handleNewOperation = () => {
        setReceivable(EMPTY_RECEIVABLE);
        setValuationDate(new Date());
        setTargetCurrencyCode('');
        setNewReceivableFormVisible(false);
        assignorSearch.reset();
        receivableHook.reset();
        receivableHook.reset();
        pricingSimulation.reset();
        batchSimulation.reset();
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
                            icon={<Icon icon="restart_alt" className="mr-2" />}
                            rounded
                            outlined
                            severity="secondary"
                            className="gap-2"
                            onClick={handleNewOperation}
                            type="button"
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
                    onAssignorSelected={handleAssignorSelected}
                />

                {assignorSearch.assignor && (
                    <>
                        <Divider />
                        <PrincingSimulationParams
                            currencies={currencies}
                            valuationDate={valuationDate}
                            onValuationDateChange={setValuationDate}
                            targetCurrencyCode={targetCurrencyCode}
                            onTargetCurrencyCodeChange={setTargetCurrencyCode}
                        />
                    </>
                )}
            </Card>

            {assignorSearch.assignor && (
                <ReceivableList
                    receivables={receivableHook.receivables}
                    loading={receivableHook.loading}
                    simulating={batchSimulation.simulating}
                    onSimulateBatch={handleSimulateBatch}
                />
            )}

            {assignorSearch.assignor && (
                <Card
                    className="mb-3"
                    title={(
                        <div className="flex justify-content-between align-items-center">
                            <span>Recebível Novo</span>
                            <Button
                                label={newReceivableFormVisible ? 'Ocultar Formulario' : 'Simular Novo Recebível'}
                                icon={<Icon icon={newReceivableFormVisible ? 'close' : 'add'} className="mr-2" />}
                                rounded
                                outlined
                                className="gap-2"
                                onClick={() => setNewReceivableFormVisible(v => !v)}
                                type="button"
                            />
                        </div>
                    )}
                >
                    {newReceivableFormVisible && (
                        <ReceivableForm
                            receivable={receivable}
                            onChange={setReceivable}
                            currencies={currencies}
                            onSimulate={handleSimulate}
                            simulating={receivableHook.saving || pricingSimulation.simulating}
                        />
                    )}
                </Card>
            )}

            {pricingSimulation.simulation && (
                <PrincingSimulationResult simulation={pricingSimulation.simulation} />
            )}

            <BatchPrincingSimulationResults results={batchSimulation.results} />
        </>
    );
};
