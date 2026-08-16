import ValidationUtils from '../../../utils/ValidationUtils.ts';

export default interface AssignorRequest {
    documentNumber: string;
    name: string;
    email: string;
    phone?: string;
}

const CNPJ_DIGITS_REGEX = /^\d{14}$/;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// O backend armazena o CNPJ apenas com digitos. Removemos a mascara antes
// de validar e antes de enviar a requisicao.
export const stripDocumentNumberMask = (documentNumber: string): string =>
    documentNumber.replace(/\D/g, '');

export const validateAssignorRequest = (request: AssignorRequest): { message: string }[] => {
    const errors: { message: string }[] = [];

    const digitsOnly = stripDocumentNumberMask(request.documentNumber);
    if (!CNPJ_DIGITS_REGEX.test(digitsOnly))
        errors.push({ message: 'O CNPJ deve possuir 14 dígitos validos.' });

    if (ValidationUtils.isBlank(request.name))
        errors.push({ message: 'A razão social do cedente e obrigatória.' });

    if (ValidationUtils.isBlank(request.email) || !EMAIL_REGEX.test(request.email))
        errors.push({ message: 'Informe um e-mail válido para o cedente.' });

    return errors;
};
