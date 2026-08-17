import { useCallback, useEffect, useState } from 'react';
import { useToast } from '../../context/ToastContext.tsx';
import type SettlementExtractResponse from '../../services/settlement/dto/SettlementExtractResponse.ts';
import type SettlementExtractFilter from '../../services/settlement/dto/SettlementExtractFilter.ts';
import { emptySettlementExtractFilter } from '../../services/settlement/dto/SettlementExtractFilter.ts';
import { settlementService } from '../../services/settlement/settlement.service.ts';
import type PageResponse from "../../services/shared/PageResponse.ts";

const PAGE_SIZE = 20;

const emptyPage: PageResponse<SettlementExtractResponse> = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: PAGE_SIZE,
};

export function useSettlementExtract() {
    const toast = useToast();
    const [filter, setFilter] = useState<SettlementExtractFilter>(emptySettlementExtractFilter);
    const [page, setPage] = useState<PageResponse<SettlementExtractResponse>>(emptyPage);
    const [loading, setLoading] = useState(false);

    const fetchPage = useCallback((pageNumber: number, currentFilter: SettlementExtractFilter) => {
        setLoading(true);
        settlementService.findExtract(currentFilter, pageNumber, PAGE_SIZE)
            .then(setPage)
            .catch(e => toast.error({ detail: e.message }))
            .finally(() => setLoading(false));
    }, [toast]);

    // Carrega a primeira pagina assim que a tela monta, sem filtro nenhum.
    useEffect(() => {
        fetchPage(0, emptySettlementExtractFilter);
    }, []);

    // Toda mudanca de filtro volta para a primeira pagina, ja que a pagina
    // atual pode nao existir mais no resultado filtrado.
    const applyFilter = useCallback((newFilter: SettlementExtractFilter) => {
        setFilter(newFilter);
        fetchPage(0, newFilter);
    }, [fetchPage]);

    const changePage = useCallback((pageNumber: number) => {
        fetchPage(pageNumber, filter);
    }, [fetchPage, filter]);

    return {
        filter,
        page,
        loading,
        pageSize: PAGE_SIZE,
        applyFilter,
        changePage,
    };
}
