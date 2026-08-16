import { http } from '../../api/client';
import DateUtils from '../../utils/DateUtils';
import type PricingSimulationResponse from './dto/PricingSimulationResponse';

export const pricingSimulationService = {

    simulate(receivableId: string, valuationDate: Date, targetCurrencyCode: string) {
        const params = new URLSearchParams({
            valuationDate: DateUtils.toLocalDateString(valuationDate),
            targetCurrencyCode,
        });

        return http.get<PricingSimulationResponse>(
            `/receivables/${receivableId}/pricing-simulation?${params.toString()}`
        );
    },

};
