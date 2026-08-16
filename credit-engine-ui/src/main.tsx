import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './assets/css/index.css'

import 'primereact/resources/themes/lara-dark-blue/theme.css';
import 'primereact/resources/primereact.css';
import 'primeflex/themes/primeone-dark.css';
import 'primeflex/primeflex.min.css';

import { addLocale, PrimeReactProvider } from 'primereact/api';
import { ToastProvider } from './context/ToastContext.tsx';
import {RouterProvider} from "react-router-dom";
import {router} from "./router/router.tsx";

addLocale('pt-BR', {
    accept: 'Sim',
    reject: 'Cancelar',
    choose: 'Escolher',
    upload: 'Enviar',
    cancel: 'Cancelar',
    dayNames: ['Domingo', 'Segunda-feira', 'Terca-feira', 'Quarta-feira', 'Quinta-feira', 'Sexta-feira', 'Sabado'],
    dayNamesMin: ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'],
    dayNamesShort: ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sab'],
    monthNames: [
        'Janeiro', 'Fevereiro', 'Marco', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
    ],
    monthNamesShort: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'],
    now: 'Agora',
    today: 'Hoje',
    weekHeader: 'Sem',
    emptyFilterMessage: 'Nenhum resultado encontrado',
    emptyMessage: 'Nenhum resultado encontrado',
});

const primeOptions = {
    locale: 'pt-BR',
    ripple: true,
    hideOverlaysOnDocumentScrolling: true,
};

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <ToastProvider>
          <PrimeReactProvider value={primeOptions}>
              <RouterProvider router={router} />
          </PrimeReactProvider>
      </ToastProvider>
  </StrictMode>,
)
