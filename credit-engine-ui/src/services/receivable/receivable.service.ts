import { http } from '../../api/client';
import DateUtils from '../../utils/DateUtils';
import type ReceivableRequest from './dto/ReceivableRequest';
import type ReceivableResponse from './dto/ReceivableResponse';

const controllerUrl = '/receivables';

// dueDate e um LocalDate no backend. Convertemos para yyyy-MM-dd aqui para
// nao serializar um Instant completo via JSON.stringify(Date).
function toPayload(request: ReceivableRequest) {
    return {
        ...request,
        currencyCode: request.currencyCode.toUpperCase(),
        dueDate: request.dueDate ? DateUtils.toLocalDateString(request.dueDate) : null,
    };
}

export const receivableService = {

    findByAssignor(assignorId: string) {
        return http.get<ReceivableResponse[]>(`${controllerUrl}?assignorId=${assignorId}`);
    },

    create(request: ReceivableRequest) {
        return http.post<ReceivableResponse>(controllerUrl, toPayload(request));
    },

    update(id: string, request: ReceivableRequest) {
        return http.put<ReceivableResponse>(`${controllerUrl}/${id}`, toPayload(request));
    },

};
