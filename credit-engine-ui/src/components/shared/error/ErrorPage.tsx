import { Button } from 'primereact/button';
import { Divider } from 'primereact/divider';
import { useNavigate, useRouteError } from 'react-router-dom';

interface RouteError {
    status?: number;
    statusText?: string;
    message?: string;
}

export default function ErrorPage() {
    const error = useRouteError() as RouteError;
    const navigate = useNavigate();

    return (
        <div className="flex align-items-center justify-content-center min-h-screen px-4">
            <div className="text-center surface-card border-round-2xl p-6 shadow-4 w-full max-w-30rem">
                <i className="pi pi-exclamation-triangle text-6xl mb-4 text-orange-400" />

                <h1 className="text-4xl font-bold mb-2">Ops!</h1>

                <p className="text-color-secondary mb-4">Algo inesperado aconteceu.</p>

                <Divider className="mb-1" />

                <div className="border-round p-3 text-center text-sm mb-5">
                    <strong>{error?.status || 500}</strong>
                    {' - '}
                    {error?.statusText || error?.message || 'Erro desconhecido'}
                </div>

                <div className="flex justify-content-center gap-3">
                    <Button label="Voltar" icon="pi pi-arrow-left" outlined onClick={() => navigate(-1)} />
                </div>
            </div>
        </div>
    );
}