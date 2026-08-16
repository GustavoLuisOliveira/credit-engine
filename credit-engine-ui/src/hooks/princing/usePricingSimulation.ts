import { useCallback, useState } from 'react';
import type PricingSimulationResponse from "../../services/pricing/dto/PricingSimulationResponse.ts";
import {useToast} from "../../context/ToastContext.tsx";
import ValidationUtils from "../../utils/ValidationUtils.ts";
import {pricingSimulationService} from "../../services/pricing/pricingSimulation.service.ts";

export interface BatchPrincingSimulationResult {
    receivableId: string;
    simulation: PricingSimulationResponse | null;
    error: string | null;
}

export function usePricingSimulation() {
    const toast = useToast();
    const [simulating, setSimulating] = useState(false);
    const [results, setResults] = useState<BatchPrincingSimulationResult[]>([]);

    const upsertResults = (updates: BatchPrincingSimulationResult[]) => {
        setResults(prev => {
            const byId = new Map(prev.map(r => [r.receivableId, r]));
            updates.forEach(update => byId.set(update.receivableId, update));
            return Array.from(byId.values());
        });
    };

    // Nao existe endpoint de simulação em lote no backend. Disparamos uma
    // chamada de simulação por recebível informado e apresentamos cada
    // resultado individualmente, sem somar os valores.
    const simulate = useCallback(async (
        receivableIds: string[],
        valuationDate: Date | null,
        targetCurrencyCode: string,
    ) => {
        if (receivableIds.length === 0) {
            toast.error({ detail: 'Selecione ao menos um recebível para simular.' });
            return;
        }

        if (!valuationDate) {
            toast.error({ detail: 'A data de referência da simulação e obrigatória.' });
            return;
        }

        if (ValidationUtils.isBlank(targetCurrencyCode)) {
            toast.error({ detail: 'Selecione a moeda de liquidação.' });
            return;
        }


        setSimulating(true);

        const settled = await Promise.all(
            receivableIds.map(receivableId =>
                pricingSimulationService.simulate(receivableId, valuationDate, targetCurrencyCode)
                    .then(simulation => ({ receivableId, simulation, error: null }))
                    .catch(e => ({ receivableId, simulation: null, error: e.message as string }))
            )
        );

        upsertResults(settled);
        setSimulating(false);

        const failedCount = settled.filter(r => r.error).length;
        if (failedCount > 0)
            toast.error({ detail: `${failedCount} recebível(is) falharam ao simular.` });
    }, [toast]);

    // Descarta resultados dos ids informados, usado quando um recebível sai da selecao no ReceivableList.
    const remove = useCallback((receivableIds: string[]) => {
        setResults(prev => prev.filter(r => !receivableIds.includes(r.receivableId)));
    }, []);

    const reset = useCallback(() => {
        setResults([]);
    }, []);

    return {
        simulating,
        results,
        simulate,
        reset,
        remove
    };
}