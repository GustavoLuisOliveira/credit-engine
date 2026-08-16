export type ApiErrorBody = {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    fields?: Record<string, string>;
};

export class ApiError extends Error {

    readonly status: number;
    readonly fields?: Record<string, string>;

    constructor(message: string, status: number, body?: ApiErrorBody) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.fields = body?.fields;
    }
}