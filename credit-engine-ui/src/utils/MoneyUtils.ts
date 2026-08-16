export default class MoneyUtils {

    // Formata um valor decimal com o codigo de moeda ISO 4217 (BRL, USD),
    // usando o locale pt-BR para separadores.
    static formatar(valor: number | null | undefined, currencyCode: string | null | undefined): string {
        if (valor === null || valor === undefined || !currencyCode) return '';

        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: currencyCode,
            minimumFractionDigits: 2,
            maximumFractionDigits: 4,
        }).format(valor);
    }

    static formatarPercentual(valor: number | null | undefined): string {
        if (valor === null || valor === undefined) return '';

        return `${valor.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 4 })}%`;
    }
}
