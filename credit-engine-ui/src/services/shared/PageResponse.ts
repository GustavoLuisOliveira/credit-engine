// Espelha o formato de Page<T> do Spring Data (subset dos campos usados no frontend).
// Genérico para ser reutilizado por qualquer endpoint paginado.
export default interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}