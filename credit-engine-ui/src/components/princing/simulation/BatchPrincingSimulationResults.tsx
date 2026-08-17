import React from 'react';
import { Card } from 'primereact/card';
import { Message } from 'primereact/message';
import {PrincingSimulationResult} from "./PrincingSimulationResult.tsx";
import type { BatchPrincingSimulationResult } from "../../../hooks/princing/usePricingSimulation.ts";
import {Loading} from "../../shared/Loading.tsx";

interface Props {
    simulating: boolean;
    results: BatchPrincingSimulationResult[];
}

export const BatchPrincingSimulationResults: React.FC<Props> = ({ simulating, results }) => {
    if (results.length === 0) return null;

    return (
        <>
            {simulating ? (<Loading/>) : (
                results.map(result => (
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
                ))
            )}
        </>
    );
};
