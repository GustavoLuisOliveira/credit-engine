import { useCallback, useState } from 'react';
import {useToast} from "../../context/ToastContext.tsx";
import type ExchangeRateResponse from "../../services/currency/dto/ExchangeRateResponse.ts";
import {exchangeRateService} from "../../services/currency/exchangeRate.service.ts";
import {ApiError} from "../../api/errors/ApiError.ts";
import type ExchangeRateRequest from "../../services/currency/dto/ExchangeRateRequest.ts";
import {validateExchangeRateRequest} from "../../services/currency/dto/ExchangeRateRequest.ts";

export function useExchangeRates() {
    const toast = useToast();
    const [loading, setLoading] = useState(false);
    const [creating, setCreating] = useState(false);
    const [searched, setSearched] = useState(false);

    const [latestRate, setLatestRate] = useState<ExchangeRateResponse | null>(null);

    const findLatestRate = useCallback((origin: string, destination: string) => {
        setLoading(true);
        setSearched(false);

        exchangeRateService.findLatestRate(origin, destination)
            .then(rate => setLatestRate(rate))
            .catch(e => {
                if (e instanceof ApiError && e.status === 404) {
                    setLatestRate(null);
                    return;
                }

                toast.error({ detail: e.message });
            })
            .finally(() => {
                setLoading(false);
                setSearched(true);
            });
    }, [toast]);

    const create = useCallback((request: ExchangeRateRequest) => {
        const errors = validateExchangeRateRequest(request);
        if (errors.length > 0) {
            errors.forEach(error => toast.error({ detail: error.message }));
            return Promise.resolve(null);
        }

        setCreating(true);
        return exchangeRateService.create(request)
            .then(created => {
                toast.success({ detail: 'Cotação registrada com sucesso!' });
                return created;
            })
            .catch(e => { toast.error({ detail: e.message }); return null; })
            .finally(() => setCreating(false));
    }, [toast]);

    const reset = useCallback(() => {
        setSearched(false);
        setLatestRate(null);
    }, []);

    return {
        latestRate,
        loading,
        creating,
        searched,
        findLatestRate,
        create,
        reset,
    };
}
