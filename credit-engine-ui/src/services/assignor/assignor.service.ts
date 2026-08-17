import { http } from '../../api/client';
import type AssignorRequest from './dto/AssignorRequest';
import type AssignorResponse from './dto/AssignorResponse';
import { stripDocumentNumberMask } from './dto/AssignorRequest';

const controllerUrl = '/assignors';

export const assignorService = {

    findAll() {
        return http.get<AssignorResponse[]>(controllerUrl);
    },

    findByDocumentNumber(documentNumber: string) {
        const digitsOnly = stripDocumentNumberMask(documentNumber);
        return http.get<AssignorResponse>(`${controllerUrl}?documentNumber=${digitsOnly}`);
    },

    create(request: AssignorRequest) {
        return http.post<AssignorResponse>(controllerUrl, {
            ...request,
            documentNumber: stripDocumentNumberMask(request.documentNumber),
        });
    },

};
