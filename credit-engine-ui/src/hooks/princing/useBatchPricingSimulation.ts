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

export function useBatchPricingSimulation() {
    const toast = useToast();
    const [simulating, setSimulating] = useState(false);
    const [results, setResults] = useState<BatchPrincingSimulationResult[]>([]);

    // Nao existe endpoint de simulacao em lote no backend. Disparamos uma
    // chamada de simulacao por recebível selecionado e apresentamos cada
    // resultado individualmente, sem somar os valores.
    const simulateBatch = useCallback(async (
        receivableIds: string[],
        valuationDate: Date | null,
        targetCurrencyCode: string,
    ) => {
        if (receivableIds.length === 0) {
            toast.error({ detail: 'Selecione ao menos um recebível para simular.' });
            return;
        }

        if (ValidationUtils.isBlank(targetCurrencyCode)) {
            toast.error({ detail: 'Selecione a moeda de liquidação.' });
            return;
        }

        if (!valuationDate) {
            toast.error({ detail: 'A data de referencia da simulacao e obrigatória.' });
            return;
        }

        setSimulating(true);
        setResults([]);

        const settled = await Promise.all(
            receivableIds.map(receivableId =>
                pricingSimulationService.simulate(receivableId, valuationDate, targetCurrencyCode)
                    .then(simulation => ({ receivableId, simulation, error: null }))
                    .catch(e => ({ receivableId, simulation: null, error: e.message as string }))
            )
        );

        setResults(settled);
        setSimulating(false);

        const failedCount = settled.filter(r => r.error).length;
        if (failedCount > 0)
            toast.error({ detail: `${failedCount} recebível(is) falharam ao simular.` });
    }, [toast]);

    const reset = useCallback(() => {
        setResults([]);
    }, []);

    return {
        simulating,
        results,
        simulateBatch,
        reset,
    };
}
