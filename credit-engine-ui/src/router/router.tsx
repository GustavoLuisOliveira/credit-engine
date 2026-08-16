import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import ErrorPage from '../components/shared/error/ErrorPage';
import { Home } from '../components/home/Home';
import {CurrencyPage} from "../components/currency/CurrencyPage.tsx";

export const router = createBrowserRouter([
    {
        path: '/',
        Component: App,
        children: [
            {
                index: true,
                element: <Home />,
            },
            {
                path: '/currencies',
                element: <CurrencyPage />,
            },
        ],
        errorElement: <ErrorPage />,
    },
]);