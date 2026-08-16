package com.credit.engine.web.controller.pricing;

import com.credit.engine.application.dto.pricing.PricingSimulationResponse;
import com.credit.engine.application.service.pricing.PricingCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Endpoint de simulação
 * alimenta a exibição em tempo real do valor líquido no Painel do Operador.
 */
@RestController
@RequestMapping("/api/receivables/{receivableId}/pricing-simulation")
@RequiredArgsConstructor
public class PricingSimulationController {

    private final PricingCalculationService pricingCalculationService;

    /**
     * @param targetCurrencyCode opcional; se informado e diferente da moeda original do
     *                            título, a resposta também traz o valor convertido usando
     *                            a cotação vigente (ex: {@code USD} para simular o
     *                            recebimento em dólar de um título emitido em BRL).
     */
    @GetMapping
    public ResponseEntity<PricingSimulationResponse> simulate(
            @PathVariable UUID receivableId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate valuationDate,
            @RequestParam(required = false) String targetCurrencyCode
    ) {
        return ResponseEntity.ok(pricingCalculationService.simulate(receivableId, valuationDate, targetCurrencyCode));
    }

}
