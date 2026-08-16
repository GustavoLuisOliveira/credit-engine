// O backend serializa LocalDate como yyyy-MM-dd e Instant como
// ISO_OFFSET_DATE_TIME no fuso America/Sao_Paulo (ex: 2026-08-16T15:37:00-03:00).
// Este reviver converte ambos os formatos em Date automaticamente ao fazer
// o parse do JSON, evitando conversao manual em cada tela.

const shortDateRegex = /^(\d{4})-(\d{2})-(\d{2})$/;
const offsetDateTimeRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[+-]\d{2}:\d{2})$/;

export function dateReviver(_key: unknown, value: unknown): unknown {

    if (typeof value !== 'string') {
        return value;
    }

    if (shortDateRegex.test(value)) {
        const date = new Date(`${value}T00:00:00`);

        if (!isNaN(date.getTime())) {
            return date;
        }
    }

    if (offsetDateTimeRegex.test(value)) {
        const date = new Date(value);

        if (!isNaN(date.getTime())) {
            return date;
        }
    }

    return value;
}