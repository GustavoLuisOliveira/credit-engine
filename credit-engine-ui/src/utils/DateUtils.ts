export default class DateUtils {

    // Converte Date para yyyy-MM-dd, formato esperado pelos parametros LocalDate do backend
    static toLocalDateString(date: Date): string {
        if (!date) {
            throw new Error('Data invalida.');
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        return `${year}-${month}-${day}`;
    }

    static formatDate(date: Date | null | undefined): string | null {
        if (!date) return null;
        return date.toLocaleDateString('pt-BR');
    }

    static formatDateTime(date: Date | null | undefined): string | null {
        if (!date) return null;

        const formattedDate = date.toLocaleDateString('pt-BR');
        const formattedTime = date.toLocaleTimeString('pt-BR', {
            hour: '2-digit',
            minute: '2-digit',
        });

        return `${formattedDate} ${formattedTime}`;
    }
}
