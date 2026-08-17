import ValidationUtils from '../../../utils/ValidationUtils.ts';
import type { ReceivableType } from '../../receivable/ReceivableType.ts';

export default interface PricingParameterRequest {
    receivableType: ReceivableType | '';
    baseRate: number | null;
    spreadRate: number | null;
    effectiveDate: Date | null;
}

export const validatePricingParameterRequest = (request: PricingParameterRequest): { message: string }[] => {
    const errors: { message: string }[] = [];

    if (ValidationUtils.isBlank(request.receivableType))
        errors.push({ message: 'O tipo de recebível é obrigatório.' });

    if (request.baseRate === null || request.baseRate < 0 || request.baseRate > 100)
        errors.push({ message: 'A taxa base deve estar entre 0 e 100.' });

    if (request.spreadRate === null || request.spreadRate < 0 || request.spreadRate > 100)
        errors.push({ message: 'O spread deve estar entre 0 e 100.' });

    if (!request.effectiveDate)
        errors.push({ message: 'A data de vigência é obrigatória.' });

    return errors;
};
