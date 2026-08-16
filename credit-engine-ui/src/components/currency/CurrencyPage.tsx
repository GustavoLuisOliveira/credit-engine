import React from "react";
import {useCurrencies} from "../../hooks/useCurrencies.ts";
import {Currencies} from "./Currencies.tsx";
import {ExchangeRates} from "./ExchangeRates.tsx";

export const CurrencyPage: React.FC = () => {
    const { currencies, loading, creating, create } = useCurrencies();

    return (
        <>
            <ExchangeRates currencies={currencies} />

            <Currencies
                currencies={currencies}
                loading={loading}
                creating={creating}
                create={create}
            />
        </>
    );
};
