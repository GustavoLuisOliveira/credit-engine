import React from 'react';
import { Card } from 'primereact/card';
import { Message } from 'primereact/message';
import {PrincingSimulationResult} from "./PrincingSimulationResult.tsx";
import type { BatchPrincingSimulationResult } from "../../hooks/princing/useBatchPricingSimulation.ts";

interface Props {
    results: BatchPrincingSimulationResult[];
}

export const BatchPrincingSimulationResults: React.FC<Props> = ({ results }) => {
    if (results.length === 0) return null;

    return (
        <>
            {results.map(result => (
                <div key={result.receivableId} className="mb-3">
                    {result.simulation ? (
                        <PrincingSimulationResult simulation={result.simulation} />
                    ) : (
                        <Card>
                            <Message
                                severity="error"
                                className="w-full justify-content-start"
                                text={`Falha ao simular o recebível ${result.receivableId}: ${result.error}`}
                            />
                        </Card>
                    )}
                </div>
            ))}
        </>
    );
};
