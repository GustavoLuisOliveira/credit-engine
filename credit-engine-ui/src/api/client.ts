import { ApiError, type ApiErrorBody } from './errors/ApiError';
import { dateReviver } from './utils/dateReviver';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

async function handleResponse<T>(response: Response): Promise<T> {

    const contentType = response.headers.get('content-type') ?? '';
    const hasBody = response.headers.get('content-length') !== '0' && response.status !== 204;

    if (response.ok) {

        if (hasBody && contentType.includes('application/json')) {
            const text = await response.text();
            return JSON.parse(text, dateReviver) as T;
        }

        return undefined as T;
    }

    let message = response.statusText || 'Erro na requisicao';
    let body: ApiErrorBody | undefined;

    try {
        if (hasBody && contentType.includes('application/json')) {
            body = await response.json();
            message = body?.message || message;
        }
    } catch {
        //
    }

    throw new ApiError(message, response.status, body);
}

async function request<T>(endpoint: string, init: RequestInit): Promise<T> {

    const response = await fetch(`${API_URL}${endpoint}`, {
        ...init,
        headers: {
            'Content-Type': 'application/json',
            ...init.headers,
        },
    });

    return handleResponse<T>(response);
}

export const http = {

    get: <T>(endpoint: string) =>
        request<T>(endpoint, { method: 'GET' }),

    post: <T>(endpoint: string, body: unknown) =>
        request<T>(endpoint, { method: 'POST', body: JSON.stringify(body) }),

    put: <T>(endpoint: string, body: unknown) =>
        request<T>(endpoint, { method: 'PUT', body: JSON.stringify(body) }),

    delete: (endpoint: string) =>
        request<void>(endpoint, { method: 'DELETE' }),
};
