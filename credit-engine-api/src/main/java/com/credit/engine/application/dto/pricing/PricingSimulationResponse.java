package com.credit.engine.application.dto.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PricingSimulationResponse(
        UUID receivableId,
        LocalDate valuationDate,
        BigDecimal baseRate,
        BigDecimal spreadRate,
        BigDecimal term,
        BigDecimal faceValue,
        BigDecimal discountAmount,
        BigDecimal presentValue,

        String currencyCode,
        String targetCurrencyCode,
        BigDecimal exchangeRateUsed,
        BigDecimal convertedAmount
) {
}
