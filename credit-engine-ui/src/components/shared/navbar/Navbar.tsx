import { Menubar } from 'primereact/menubar';
import type { MenuItem } from 'primereact/menuitem';
import { useNavigate } from 'react-router-dom';
import {Icon} from "../Icon.tsx";

export const Navbar = () => {
    const navigate = useNavigate();

    const items: MenuItem[] = [
        {
            label: 'Painel do Operador',
            icon: <Icon icon={'payments'} className={'mr-2'} />,
            command: () => navigate('/'),
        },
        {
            label: 'Moedas e Câmbio',
            icon: <Icon icon={'currency_exchange'} className={'mr-2'} />,
            command: () => navigate('/currencies'),
        },
    ];

    return (
        <Menubar
            model={items}
            start={
                <span className="font-bold text-lg mr-4 white-space-nowrap">
                    SRM Credit Engine
                </span>
            }
        />
    );
}