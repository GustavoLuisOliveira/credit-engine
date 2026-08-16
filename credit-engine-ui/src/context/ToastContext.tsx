import React, { createContext, useContext, useMemo, useRef } from 'react';
import { Toast } from 'primereact/toast';

/**
 * Tipo usado para simplificar os parâmetros de cada toast.
 * - summary: título (opcional)
 * - detail: mensagem detalhada (obrigatória)
 * - life: tempo de exibição em ms
 */
type ToastPayload = {
    summary?: string;
    detail: string;
    life?: number;
};

/**
 * API exposta pelo ToastProvider para consumo no app.
 * Fornece métodos de alto nível (success, info, warn, error)
 * e também um método show genérico de baixo nível.
 */
type ToastApi = {
    success: (p: ToastPayload) => void;
    info: (p: ToastPayload) => void;
    warn: (p: ToastPayload) => void;
    error: (p: ToastPayload) => void;
    // Método genérico para casos de uso avançados
    show: (args: {
        severity: 'success' | 'info' | 'warn' | 'error';
        summary?: string;
        detail: string;
        life?: number;
    }) => void;
};

/**
 * Contexto que armazenará a API do Toast.
 * Iniciado como null e será preenchido pelo ToastProvider.
 */
const ToastContext = createContext<ToastApi | null>(null);

/**
 * ToastProvider é o componente de mais alto nível que:
 * - Renderiza o <Toast> do PrimeReact
 * - Expõe funções de disparo via contexto (useToast)
 */
export const ToastProvider: React.FC<React.PropsWithChildren> = ({ children }) => {
    // useRef mantém a referência para o componente Toast do PrimeReact
    const ref = useRef<Toast>(null);

    /**
     * useMemo garante que a API só é recriada uma vez.
     * As funções internamente chamam o Toast do PrimeReact.
     */
    const api = useMemo<ToastApi>(
        () => ({
            //  Método genérico (baixo nível)
            show: ({ severity, summary, detail, life }) => {
                ref.current?.show({ severity, summary, detail, life });
            },

            // Métodos de alto nível, com severidade fixa e defaults
            success: ({ summary = 'Sucesso', detail, life = 3000 }) =>
                ref.current?.show({ severity: 'success', summary, detail, life }),

            info: ({ summary = 'Info', detail, life = 4000 }) =>
                ref.current?.show({ severity: 'info', summary, detail, life }),

            warn: ({ summary = 'Atenção', detail, life = 5000 }) =>
                ref.current?.show({ severity: 'warn', summary, detail, life }),

            error: ({ summary = 'Erro', detail, life = 6000 }) =>
                ref.current?.show({ severity: 'error', summary, detail, life }),
        }),
        []
    );

    return (
        <ToastContext.Provider value={api}>
            {/* O Toast do PrimeReact é renderizado aqui, disponível para toda a aplicação */}
            <Toast ref={ref} position="top-right" />
            {children}
        </ToastContext.Provider>
    );
};

/**
 * Hook customizado para acessar o Toast em qualquer componente.
 * Garante que só pode ser usado dentro de um ToastProvider.
 */
export const useToast = (): ToastApi => {
    const ctx = useContext(ToastContext);
    if (!ctx) throw new Error('useToast precisa estar dentro de contexto.');
    return ctx;
};
