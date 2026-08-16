import { http } from '../../api/client';
import type CurrencyRequest from './dto/CurrencyRequest';
import type CurrencyResponse from './dto/CurrencyResponse';

const controllerUrl = '/currencies';

export const currencyService = {

    findAll() {
        return http.get<CurrencyResponse[]>(controllerUrl);
    },

    findByCode(code: string) {
        return http.get<CurrencyResponse>(`${controllerUrl}/${code}`);
    },

    create(request: CurrencyRequest) {
        return http.post<CurrencyResponse>(controllerUrl, request);
    },

};
