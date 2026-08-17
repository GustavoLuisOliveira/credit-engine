import React from 'react';
import { Card } from 'primereact/card';
import { RECEIVABLE_TYPE_OPTIONS } from '../../services/receivable/ReceivableType.ts';
import type { ReceivableType } from '../../services/receivable/ReceivableType.ts';
import type PricingParameterResponse from '../../services/pricing/dto/PricingParameterResponse.ts';
import { Loading } from '../shared/Loading.tsx';
import DateUtils from '../../utils/DateUtils.ts';
import {EmptyState} from "../shared/EmptyState.tsx";

interface Props {
    current: Partial<Record<ReceivableType, PricingParameterResponse | null>>;
    loading: boolean;
}

export const CurrentPricingParameters: React.FC<Props> = ({ current, loading }) => {
    if (loading) return <Loading />;

    return (
        <div className="grid">
            {RECEIVABLE_TYPE_OPTIONS.map(option => {
                const type = option.value as ReceivableType;
                const parameter = current[type];

                return (
                    <div key={type} className="col-12 md:col-6">
                        <Card title={option.label} className="h-full">
                            {parameter ? (
                                <div className="flex flex-column gap-2 text-sm">
                                    <div className="flex justify-content-between">
                                        <span className="text-color-secondary">Taxa base</span>
                                        <span className="font-semibold">{parameter.baseRate}% a.m.</span>
                                    </div>
                                    <div className="flex justify-content-between">
                                        <span className="text-color-secondary">Spread</span>
                                        <span className="font-semibold">{parameter.spreadRate}% a.m.</span>
                                    </div>
                                    <div className="flex justify-content-between">
                                        <span className="text-color-secondary">Vigente desde</span>
                                        <span className="font-semibold">{DateUtils.formatDate(parameter.effectiveDate)}</span>
                                    </div>
                                </div>
                            ) : (
                                <EmptyState message={'Nenhum parâmetro cadastrado para este tipo.'} />
                            )}
                        </Card>
                    </div>
                );
            })}
        </div>
    );
};
