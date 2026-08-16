import { http } from '../../api/client';
import type ExchangeRateRequest from './dto/ExchangeRateRequest';
import type ExchangeRateResponse from './dto/ExchangeRateResponse';

const controllerUrl = '/exchange-rates';

export const exchangeRateService = {

    create(request: ExchangeRateRequest) {
        return http.post<ExchangeRateResponse>(controllerUrl, request);
    },

    findLatestRate(origin: string, destination: string) {
        return http.get<ExchangeRateResponse>(
            `${controllerUrl}/latest?origin=${origin}&destination=${destination}`
        );
    },

};
