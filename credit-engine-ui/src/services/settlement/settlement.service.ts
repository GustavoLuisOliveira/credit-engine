import { http } from '../../api/client';
import DateUtils from '../../utils/DateUtils';
import type SettlementRequest from './dto/SettlementRequest';
import type SettlementResponse from './dto/SettlementResponse';
import type SettlementExtractFilter from "./dto/SettlementExtractFilter.ts";
import type PageResponse from "../shared/PageResponse.ts";
import type SettlementExtractResponse from "./dto/SettlementExtractResponse.ts";

const controllerUrl = '/settlements';

function toPayload(request: SettlementRequest) {
    return {
        ...request,
        targetCurrencyCode: request.targetCurrencyCode.toUpperCase(),
        valuationDate: DateUtils.toLocalDateString(request.valuationDate),
    };
}

function toExtractParams(filter: SettlementExtractFilter, page: number, size: number): URLSearchParams {
    const params = new URLSearchParams();

    if (filter.assignorId) params.set('assignorId', filter.assignorId);
    if (filter.currencyCode) params.set('currencyCode', filter.currencyCode.toUpperCase());
    if (filter.valuationDateFrom) params.set('valuationDateFrom', DateUtils.toLocalDateString(filter.valuationDateFrom));
    if (filter.valuationDateTo) params.set('valuationDateTo', DateUtils.toLocalDateString(filter.valuationDateTo));

    params.set('page', String(page));
    params.set('size', String(size));

    return params;
}

export const settlementService = {

    execute(request: SettlementRequest) {
        return http.post<SettlementResponse>(controllerUrl, toPayload(request));
    },

    findById(id: string) {
        return http.get<SettlementResponse>(`${controllerUrl}/${id}`);
    },

    findExtract(filter: SettlementExtractFilter, page: number, size: number) {
        const params = toExtractParams(filter, page, size);
        return http.get<PageResponse<SettlementExtractResponse>>(`${controllerUrl}/extract?${params.toString()}`);
    },

};
