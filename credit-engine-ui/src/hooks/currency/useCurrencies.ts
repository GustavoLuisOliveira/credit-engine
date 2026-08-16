import { useCallback, useEffect, useState } from 'react';
import {useToast} from "../../context/ToastContext.tsx";
import type CurrencyResponse from "../../services/currency/dto/CurrencyResponse.ts";
import {currencyService} from "../../services/currency/currency.service.ts";
import type CurrencyRequest from "../../services/currency/dto/CurrencyRequest.ts";
import {validateCurrencyRequest} from "../../services/currency/dto/CurrencyRequest.ts";

export function useCurrencies() {
    const toast = useToast();
    const [loading, setLoading] = useState(false);
    const [creating, setCreating] = useState(false);

    const [currencies, setCurrencies] = useState<CurrencyResponse[]>([]);

    const findAll = useCallback(() => {
        setLoading(true);
        currencyService.findAll()
            .then(setCurrencies)
            .catch(e => toast.error({ detail: e.message }))
            .finally(() => setLoading(false));
    }, [toast]);

    const create = useCallback((request: CurrencyRequest) => {
        const errors = validateCurrencyRequest(request);
        if (errors.length > 0) {
            errors.forEach(error => toast.error({ detail: error.message }));
            return Promise.resolve(null);
        }

        setCreating(true);
        return currencyService.create({ ...request, code: request.code.toUpperCase() })
            .then(created => {
                toast.success({ detail: 'Moeda cadastrada com sucesso!' });
                setCurrencies(prev => [created, ...prev]);
                return created;
            })
            .catch(e => { toast.error({ detail: e.message }); return null; })
            .finally(() => setCreating(false));
    }, [toast]);

    useEffect(() => {
        findAll();
    }, [findAll]);

    return {
        currencies,
        loading,
        creating,
        findAll,
        create,
    };
}
