export default class ValidationUtils {

    static isBlank(value: string | null | undefined): boolean {
        return value === null || value === undefined || value.trim().length === 0;
    }
}
