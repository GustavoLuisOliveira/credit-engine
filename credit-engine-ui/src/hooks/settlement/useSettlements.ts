import { useCallback, useState } from 'react';
import { useToast } from '../../context/ToastContext.tsx';
import type SettlementRequest from '../../services/settlement/dto/SettlementRequest.ts';
import {validateSettlementRequest} from '../../services/settlement/dto/SettlementRequest.ts';
import type SettlementResponse from '../../services/settlement/dto/SettlementResponse.ts';
import { settlementService } from '../../services/settlement/settlement.service.ts';

export function useSettlements() {
    const toast = useToast();
    const [loading, setLoading] = useState(false);
    const [executing, setExecuting] = useState(false);
    const [settlement, setSettlement] = useState<SettlementResponse | null>(null);

    // Recebe os mesmos parametros usados na simulacao (cedente, recebiveis,
    // data de referencia e moeda alvo). O backend recalcula a precificacao
    // no momento da liquidacao, nao reaproveita o resultado ja simulado.
    const execute = useCallback((
        assignorId: string,
        receivableIds: string[],
        valuationDate: Date,
        targetCurrencyCode: string,
    ): Promise<SettlementResponse | null> => {
        const request: SettlementRequest = { assignorId, receivableIds, valuationDate, targetCurrencyCode };

        const errors = validateSettlementRequest(request);
        if (errors.length > 0) {
            errors.forEach(error => toast.error({ detail: error.message }));
            return Promise.resolve(null);
        }

        setExecuting(true);
        return settlementService.execute(request)
            .then(created => {
                toast.success({ detail: 'Liquidação executada com sucesso.' });
                setSettlement(created);
                return created;
            })
            .catch(e => {
                toast.error({ detail: e.message });
                return null;
            })
            .finally(() => setExecuting(false));
    }, [toast]);

    const findById = useCallback((id: string) => {
        setLoading(true);
        return settlementService.findById(id)
            .then(found => {
                setSettlement(found);
                return found;
            })
            .catch(e => {
                toast.error({ detail: e.message });
                return null;
            })
            .finally(() => setLoading(false));
    }, [toast]);

    const closeResult = useCallback(() => {
        setSettlement(null);
    }, []);

    return {
        executing,
        settlement,
        execute,
        closeResult,
        findById,
        loading
    };
}
