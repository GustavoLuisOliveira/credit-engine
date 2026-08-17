import { useCallback, useEffect, useState } from 'react';
import { useToast } from '../../context/ToastContext.tsx';
import { pricingParameterService } from '../../services/pricing/pricingParameter.service.ts';
import type PricingParameterRequest from '../../services/pricing/dto/PricingParameterRequest.ts';
import { validatePricingParameterRequest } from '../../services/pricing/dto/PricingParameterRequest.ts';
import type PricingParameterResponse from '../../services/pricing/dto/PricingParameterResponse.ts';
import { ApiError } from '../../api/errors/ApiError.ts';
import type { ReceivableType } from '../../services/receivable/ReceivableType.ts';
import { RECEIVABLE_TYPE_OPTIONS } from '../../services/receivable/ReceivableType.ts';

type CurrentByType = Partial<Record<ReceivableType, PricingParameterResponse | null>>;

export function usePricingParameters() {
    const toast = useToast();
    const [loadingCurrent, setLoadingCurrent] = useState(false);
    const [loadingHistory, setLoadingHistory] = useState(false);
    const [creating, setCreating] = useState(false);

    const [current, setCurrent] = useState<CurrentByType>({});
    const [history, setHistory] = useState<PricingParameterResponse[]>([]);
    const [historyType, setHistoryType] = useState<ReceivableType>(
        RECEIVABLE_TYPE_OPTIONS[0].value as ReceivableType
    );

    const findAllCurrent = useCallback(() => {
        setLoadingCurrent(true);

        Promise.all(
            RECEIVABLE_TYPE_OPTIONS.map(option => {
                const type = option.value as ReceivableType;

                return pricingParameterService.findCurrent(type)
                    .then(response => ({ type, response }))
                    .catch(e => {
                        if (e instanceof ApiError && e.status === 404) {
                            return { type, response: null };
                        }
                        throw e;
                    });
            })
        )
            .then(results => {
                const next: CurrentByType = {};
                results.forEach(({ type, response }) => { next[type] = response; });
                setCurrent(next);
            })
            .catch(e => toast.error({ detail: e.message }))
            .finally(() => setLoadingCurrent(false));
    }, [toast]);

    const findHistory = useCallback((receivableType: ReceivableType) => {
        setLoadingHistory(true);
        pricingParameterService.findHistory(receivableType)
            .then(setHistory)
            .catch(e => toast.error({ detail: e.message }))
            .finally(() => setLoadingHistory(false));
    }, [toast]);

    const changeHistoryType = useCallback((receivableType: ReceivableType) => {
        setHistoryType(receivableType);
    }, []);

    const create = useCallback((request: PricingParameterRequest) => {
        const errors = validatePricingParameterRequest(request);
        if (errors.length > 0) {
            errors.forEach(error => toast.error({ detail: error.message }));
            return Promise.resolve(null);
        }

        setCreating(true);
        return pricingParameterService.create(request)
            .then(created => {
                toast.success({ detail: 'Parametro de precificacao cadastrado com sucesso!' });
                findAllCurrent();
                if (created.receivableType === historyType) findHistory(historyType);
                return created;
            })
            .catch(e => { toast.error({ detail: e.message }); return null; })
            .finally(() => setCreating(false));
    }, [toast, findAllCurrent, findHistory, historyType]);

    useEffect(() => {
        findAllCurrent();
    }, [findAllCurrent]);

    useEffect(() => {
        findHistory(historyType);
    }, [historyType, findHistory]);

    return {
        current,
        loadingCurrent,
        history,
        loadingHistory,
        historyType,
        changeHistoryType,
        creating,
        create,
    };
}
