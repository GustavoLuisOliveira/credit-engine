package com.credit.engine.application.dto.pricing;

import com.credit.engine.domain.model.pricing.PricingParameter;
import com.credit.engine.domain.model.receivable.ReceivableType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PricingParameterResponse(
        UUID id,
        ReceivableType receivableType,
        BigDecimal baseRate,
        BigDecimal spreadRate,
        LocalDate effectiveDate,
        Instant createdAt,
        Instant updatedAt
) {
    public static PricingParameterResponse toResponse(PricingParameter domain) {
        return new PricingParameterResponse(
                domain.getId(),
                domain.getReceivableType(),
                domain.getBaseRate(),
                domain.getSpreadRate(),
                domain.getEffectiveDate(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
