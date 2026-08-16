import { useCallback, useState } from 'react';
import {useToast} from "../../context/ToastContext.tsx";
import type ReceivableRequest from "../../services/receivable/dto/ReceivableRequest.ts";
import {validateReceivableRequest} from "../../services/receivable/dto/ReceivableRequest.ts";
import type ReceivableResponse from "../../services/receivable/dto/ReceivableResponse.ts";
import {receivableService} from "../../services/receivable/receivable.service.ts";

export function useReceivables() {
    const toast = useToast();
    const [saving, setSaving] = useState(false);
    const [loading, setLoading] = useState(false);
    const [receivables, setReceivables] = useState<ReceivableResponse[]>([]);

    // Guarda o id do recebível apos o primeiro salvamento. Enquanto o
    // operador estiver ajustando a mesma operacao, chamadas seguintes fazem
    // update (PUT) no lugar de criar um novo registro a cada simulacao.
    const [receivableId, setReceivableId] = useState<string | null>(null);

    const findByAssignor = useCallback((assignorId: string) => {
        setLoading(true);
        receivableService.findByAssignor(assignorId)
            .then(setReceivables)
            .catch(e => toast.error({ detail: e.message }))
            .finally(() => setLoading(false));
    }, [toast]);

    const save = useCallback((request: ReceivableRequest): Promise<ReceivableResponse | null> => {
        const errors = validateReceivableRequest(request);
        if (errors.length > 0) {
            errors.forEach(error => toast.error({ detail: error.message }));
            return Promise.resolve(null);
        }
        setSaving(true);
        const persist = receivableId
            ? receivableService.update(receivableId, request)
            : receivableService.create(request);
        return persist
            .then(receivable => {
                setReceivableId(receivable.id);
                return receivable;
            })
            .catch(e => {
                toast.error({ detail: e.message });
                return null;
            })
            .finally(() => setSaving(false));
    }, [receivableId, toast]);

    const reset = useCallback(() => {
        setReceivableId(null);
        setReceivables([]);
    }, []);

    return {
        saving,
        loading,
        receivables,
        receivableId,
        findByAssignor,
        save,
        reset,
    };
}