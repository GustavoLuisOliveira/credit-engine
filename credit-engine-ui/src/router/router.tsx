import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import ErrorPage from '../components/shared/error/ErrorPage';
import {CurrencyPage} from "../components/currency/CurrencyPage.tsx";
import {OperatorPanel} from "../components/operatorPanel/OperatorPanelPage.tsx";
import {SettlementExtractPage} from "../components/settlement/SettlementExtractPage.tsx";

export const router = createBrowserRouter([
    {
        path: '/',
        Component: App,
        children: [
            {
                index: true,
                element: <OperatorPanel />,
            },
            {
                path: '/currencies',
                element: <CurrencyPage />,
            },
            {
                path: '/settlements/extract',
                element: <SettlementExtractPage />,
            },
        ],
        errorElement: <ErrorPage />,
    },
]);