import React from 'react';
import { Card } from 'primereact/card';
import { Divider } from 'primereact/divider';
import type PricingSimulationResponse from '../../services/pricing/dto/PricingSimulationResponse.ts';
import MoneyUtils from '../../utils/MoneyUtils.ts';

interface Props {
    simulation: PricingSimulationResponse;
}

export const PrincingSimulationResult: React.FC<Props> = ({ simulation }) => {
    // O backend pode nao preencher targetCurrencyCode/convertedAmount quando a
    // liquidação ocorre na mesma moeda do titulo (same-currency). Nesses casos
    // usamos a moeda e o valor presente do proprio titulo como fallback, em
    // vez de exibir campos vazios.
    const targetCurrencyCode = simulation.targetCurrencyCode || simulation.currencyCode;
    const convertedAmount = simulation.convertedAmount ?? simulation.presentValue;
    const isCrossCurrency = targetCurrencyCode !== simulation.currencyCode;

    return (
        <Card title="Resultado da Simulacao" className="mb-3">
            <div className="grid">
                <div className="col-6 md:col-3">
                    <span className="block text-color-secondary text-sm">Valor de Face</span>
                    <span className="font-bold">{MoneyUtils.formatar(simulation.faceValue, simulation.currencyCode)}</span>
                </div>

                <div className="col-6 md:col-3">
                    <span className="block text-color-secondary text-sm">Prazo</span>
                    <span className="font-bold">{simulation.termMonths} mes(es)</span>
                </div>

                <div className="col-6 md:col-3">
                    <span className="block text-color-secondary text-sm">Taxa Base</span>
                    <span className="font-bold">{MoneyUtils.formatarPercentual(simulation.baseRate)}</span>
                </div>

                <div className="col-6 md:col-3">
                    <span className="block text-color-secondary text-sm">Spread</span>
                    <span className="font-bold">{MoneyUtils.formatarPercentual(simulation.spreadRate)}</span>
                </div>

                <div className="col-6 md:col-3">
                    <span className="block text-color-secondary text-sm">Deságio</span>
                    <span className="font-bold text-red-400">
                        - {MoneyUtils.formatar(simulation.discountAmount, simulation.currencyCode)}
                    </span>
                </div>

                <div className="col-6 md:col-3">
                    <span className="block text-color-secondary text-sm">Valor Presente</span>
                    <span className="font-bold">{MoneyUtils.formatar(simulation.presentValue, simulation.currencyCode)}</span>
                </div>

                {isCrossCurrency && (
                    <div className="col-6 md:col-3">
                        <span className="block text-color-secondary text-sm">
                            Cambio ({simulation.currencyCode} para {targetCurrencyCode})
                        </span>
                        <span className="font-bold">{simulation.exchangeRateUsed}</span>
                    </div>
                )}

                <div className="col-12 md:col-3">
                    <Divider className="md:hidden" />
                    <span className="block text-color-secondary text-sm">Valor Líquido a Receber</span>
                    <span className="font-bold text-green-400 text-xl">
                        {MoneyUtils.formatar(convertedAmount, targetCurrencyCode)}
                    </span>
                </div>
            </div>
        </Card>
    );
};
