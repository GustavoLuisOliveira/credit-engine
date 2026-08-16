import ValidationUtils from '../../../utils/ValidationUtils.ts';

export default interface ExchangeRateRequest {
    originCurrencyCode: string;
    destinationCurrencyCode: string;
    rate: number | null;
    rateDateTime: Date | null;
}

export const validateExchangeRateRequest = (request: ExchangeRateRequest): { message: string }[] => {
    const errors: { message: string }[] = [];

    if (ValidationUtils.isBlank(request.originCurrencyCode))
        errors.push({ message: 'A moeda de origem é obrigatória.' });

    if (ValidationUtils.isBlank(request.destinationCurrencyCode))
        errors.push({ message: 'A moeda de destino é obrigatória.' });

    if (request.rate === null || request.rate <= 0)
        errors.push({ message: 'A taxa de câmbio deve ser maior que zero.' });

    if (!request.rateDateTime)
        errors.push({ message: 'A data/hora da cotação é obrigatória.' });
    else if (request.rateDateTime.getTime() > Date.now())
        errors.push({ message: 'A data/hora da cotação não pode estar no futuro.' });

    return errors;
};
