package com.credit.engine.application.service.pricing;

import com.credit.engine.application.dto.pricing.PricingSimulationResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface PricingCalculationService {

    /**
     * Simula a precificação de um recebível numa data de liquidação hipotética, sem persistir nada.
     * É o motor por trás do "Painel do Operador" (exibição em tempo real do valor líquido).
     *
     * @param receivableId       ID do recebível a ser precificado.
     * @param settlementDate     Data de liquidação hipotética para o cálculo do deságio.
     * @param targetCurrencyCode (Opcional) Código da moeda de destino.
     *                           Se informado e diferente da moeda do título, a simulação retornará o valor convertido via cotação vigente.
     * @return DTO com os detalhes da simulação de precificação realizada.
     */
    PricingSimulationResponse simulate(UUID receivableId, LocalDate settlementDate, String targetCurrencyCode);

}
