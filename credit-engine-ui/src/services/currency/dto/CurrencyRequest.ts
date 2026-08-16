import ValidationUtils from '../../../utils/ValidationUtils.ts';

export default interface CurrencyRequest {
    code: string;
    name: string;
    symbol: string;
}

export const validateCurrencyRequest = (request: CurrencyRequest): { message: string }[] => {
    const errors: { message: string }[] = [];

    if (ValidationUtils.isBlank(request.code) || request.code.trim().length !== 3)
        errors.push({ message: 'O código da moeda deve possuir exatamente 3 letras (ex: BRL, USD).' });

    if (ValidationUtils.isBlank(request.name))
        errors.push({ message: 'O nome da moeda e obrigatório.' });

    if (ValidationUtils.isBlank(request.symbol))
        errors.push({ message: 'O simbolo da moeda e obrigatório.' });

    return errors;
};
