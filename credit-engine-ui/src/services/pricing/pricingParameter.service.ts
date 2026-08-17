import { http } from '../../api/client';
import DateUtils from '../../utils/DateUtils';
import type PricingParameterRequest from './dto/PricingParameterRequest';
import type PricingParameterResponse from './dto/PricingParameterResponse';
import type { ReceivableType } from '../receivable/ReceivableType';

const controllerUrl = '/pricing-parameters';

export const pricingParameterService = {

    create(request: PricingParameterRequest) {
        return http.post<PricingParameterResponse>(controllerUrl, {
            ...request,
            effectiveDate: request.effectiveDate ? DateUtils.toLocalDateString(request.effectiveDate) : null,
        });
    },

    findCurrent(receivableType: ReceivableType) {
        return http.get<PricingParameterResponse>(`${controllerUrl}/${receivableType}/current`);
    },

    findHistory(receivableType: ReceivableType) {
        return http.get<PricingParameterResponse[]>(`${controllerUrl}/${receivableType}/history`);
    },

};
