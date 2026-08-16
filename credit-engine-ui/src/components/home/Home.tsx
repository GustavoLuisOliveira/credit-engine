import { Card } from 'primereact/card';

export const Home: React.FC = () => {
    return (
        <Card title="SRM Credit Engine">
            <p className="text-color-secondary m-0">
                Scaffold do frontend concluido. As telas de dominio (Cedentes, Recebiveis,
                Precificacao, Liquidacao) serao adicionadas feature por feature nas proximas
                branches.
            </p>
        </Card>
    );
};