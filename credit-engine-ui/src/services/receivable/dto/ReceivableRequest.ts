import ValidationUtils from '../../../utils/ValidationUtils.ts';
import type { ReceivableType } from '../ReceivableType.ts';

export default interface ReceivableRequest {
    assignorId: string;
    type: ReceivableType | '';
    documentNumber: string;
    faceValue: number | null;
    currencyCode: string;
    dueDate: Date | null;
}

export const validateReceivableRequest = (request: ReceivableRequest): { message: string }[] => {
    const errors: { message: string }[] = [];

    if (ValidationUtils.isBlank(request.assignorId))
        errors.push({ message: 'Selecione um cedente antes de simular.' });

    if (ValidationUtils.isBlank(request.type))
        errors.push({ message: 'O tipo do recebível e obrigatório.' });

    if (ValidationUtils.isBlank(request.documentNumber))
        errors.push({ message: 'O numero do documento do titulo e obrigatório.' });

    if (request.faceValue === null || request.faceValue <= 0)
        errors.push({ message: 'O valor de face deve ser maior que zero.' });

    if (ValidationUtils.isBlank(request.currencyCode))
        errors.push({ message: 'A moeda do titulo e obrigatória.' });

    if (!request.dueDate)
        errors.push({ message: 'A data de vencimento e obrigatória.' });

    return errors;
};
