import { createBrowserRouter } from 'react-router-dom';
import App from '../App';
import ErrorPage from '../components/shared/error/ErrorPage';
import { Home } from '../components/home/Home';

export const router = createBrowserRouter([
    {
        path: '/',
        Component: App,
        children: [
            {
                index: true,
                element: <Home />,
            },
        ],
        errorElement: <ErrorPage />,
    },
]);