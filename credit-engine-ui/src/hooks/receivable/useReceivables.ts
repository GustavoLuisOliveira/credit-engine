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

    const findByAssignor = useCallback((assignorId: string) => {
        setLoading(true);
        receivableService.findByAssignor(assignorId)
            .then(setReceivables)
            .catch(e => toast.error({ detail: e.message }))
            .finally(() => setLoading(false));
    }, [toast]);

    // receivableId ausente ou null cria um recebível novo (POST). Informado,
    // atualiza o recebível existente (PUT). O hook nao guarda mais nenhum id
    // de rascunho internamente, quem chama decide qual operacao fazer.
    const save = useCallback((
        request: ReceivableRequest,
        receivableId?: string | null,
    ): Promise<ReceivableResponse | null> => {
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
            .catch(e => {
                toast.error({ detail: e.message });
                return null;
            })
            .finally(() => setSaving(false));
    }, [toast]);

    const reset = useCallback(() => {
        setReceivables([]);
    }, []);

    return {
        saving,
        loading,
        receivables,
        findByAssignor,
        save,
        reset,
    };
}