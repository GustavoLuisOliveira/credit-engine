import { http } from '../../api/client';
import DateUtils from '../../utils/DateUtils';
import type SettlementRequest from './dto/SettlementRequest';
import type SettlementResponse from './dto/SettlementResponse';

const controllerUrl = '/settlements';

function toPayload(request: SettlementRequest) {
    return {
        ...request,
        targetCurrencyCode: request.targetCurrencyCode.toUpperCase(),
        valuationDate: DateUtils.toLocalDateString(request.valuationDate),
    };
}

export const settlementService = {

    execute(request: SettlementRequest) {
        return http.post<SettlementResponse>(controllerUrl, toPayload(request));
    },

};
