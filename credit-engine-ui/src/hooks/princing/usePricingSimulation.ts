import { useCallback, useState } from 'react';
import {useToast} from "../../context/ToastContext.tsx";
import type PricingSimulationResponse from "../../services/pricing/dto/PricingSimulationResponse.ts";
import ValidationUtils from "../../utils/ValidationUtils.ts";
import {pricingSimulationService} from "../../services/pricing/pricingSimulation.service.ts";

export function usePricingSimulation() {
    const toast = useToast();
    const [simulating, setSimulating] = useState(false);
    const [simulation, setSimulation] = useState<PricingSimulationResponse | null>(null);

    const simulate = useCallback((
        receivableId: string,
        valuationDate: Date | null,
        targetCurrencyCode: string,
    ): Promise<PricingSimulationResponse | null> => {
        if (ValidationUtils.isBlank(targetCurrencyCode)) {
            toast.error({ detail: 'Selecione a moeda de liquidação.' });
            return Promise.resolve(null);
        }

        if (!valuationDate) {
            toast.error({ detail: 'A data de referência da simulação e obrigatória.' });
            return Promise.resolve(null);
        }

        setSimulating(true);
        return pricingSimulationService.simulate(receivableId, valuationDate, targetCurrencyCode)
            .then(result => {
                console.log(result);
                setSimulation(result);
                return result;
            })
            .catch(e => {
                toast.error({ detail: e.message });
                return null;
            })
            .finally(() => setSimulating(false));
    }, [toast]);

    const reset = useCallback(() => {
        setSimulation(null);
    }, []);

    return {
        simulating,
        simulation,
        simulate,
        reset,
    };
}
