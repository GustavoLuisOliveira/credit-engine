import React from 'react';
import { confirmDialog } from 'primereact/confirmdialog';
import { SaveButton } from '../shared/btns/SaveButton.tsx';
import type { BatchPrincingSimulationResult } from '../../hooks/princing/usePricingSimulation.ts';
import type SettlementResponse from '../../services/settlement/dto/SettlementResponse.ts';

interface Props {
    assignorId: string;
    valuationDate: Date;
    targetCurrencyCode: string;
    results: BatchPrincingSimulationResult[];
    executing: boolean;
    execute: (
        assignorId: string,
        receivableIds: string[],
        valuationDate: Date,
        targetCurrencyCode: string,
    ) => Promise<SettlementResponse | null>;
    onSettled: () => void;
}

export const SettlementActions: React.FC<Props> = ({
                                                       assignorId,
                                                       valuationDate,
                                                       targetCurrencyCode,
                                                       results,
                                                       executing,
                                                       execute,
                                                       onSettled,
                                                   }) => {
    // Apenas recebiveis com simulacao valida entram na liquidacao. Um item
    // que falhou na simulacao fica visivel na tela, mas nao e enviado.
    const receivableIds = results
        .filter(result => result.simulation !== null)
        .map(result => result.receivableId);

    if (receivableIds.length === 0) return null;

    const handleExecute = () => {
        confirmDialog({
            message: `Confirma a liquidação de ${receivableIds.length} ${receivableIds.length > 1 ? 'recebíveis' : 'recebível'}? Esta ação e irreversível.`,
            header: 'Confirmar Liquidação',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Liquidar',
            rejectLabel: 'Cancelar',
            accept: async () => {
                const settled = await execute(assignorId, receivableIds, valuationDate, targetCurrencyCode);
                if (settled) onSettled();
            },
        });
    };

    return (
        <div className="flex justify-content-end mb-3">
            <SaveButton
                label={`Liquidar ${receivableIds.length} ${receivableIds.length > 1 ? 'Recebíveis' : 'Recebível'}`}
                onClick={handleExecute}
                loading={executing}
            />
        </div>
    );
};
