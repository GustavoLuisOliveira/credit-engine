import ValidationUtils from "../../../utils/ValidationUtils.ts";

export default interface SettlementRequest {
    assignorId: string;
    valuationDate: Date;
    targetCurrencyCode: string;
    receivableIds: string[];
}

export const validateSettlementRequest = (request: SettlementRequest): { message: string }[] => {
    const errors: { message: string }[] = [];

    if (ValidationUtils.isBlank(request.assignorId))
        errors.push({ message: 'Cedente inválido.' });

    if (request.receivableIds.length === 0) {
        errors.push({ message: 'Selecione ao menos um recebível para liquidar.' });
    }

    if (!request.valuationDate) {
        errors.push({ message: 'A data de referência da liquidação e obrigatória.' });
    }

    if (ValidationUtils.isBlank(request.targetCurrencyCode)) {
        errors.push({ message: 'Selecione a moeda de liquidação.' });
    }

    return errors;
};