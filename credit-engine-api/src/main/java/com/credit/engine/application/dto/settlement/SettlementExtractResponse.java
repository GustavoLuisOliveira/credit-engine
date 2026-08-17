package com.credit.engine.application.dto.settlement;

import com.credit.engine.infrastructure.persistence.repository.settlement.projections.SettlementExtractProjection;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SettlementExtractResponse(
        UUID id,
        UUID assignorId,
        String assignorName,
        String assignorDocumentNumber,
        Instant settlementDateTime,
        LocalDate valuationDate,
        String targetCurrencyCode,
        BigDecimal totalFaceValue,
        BigDecimal totalDiscountAmount,
        BigDecimal totalNetAmount
) {
    public static SettlementExtractResponse toResponse(SettlementExtractProjection projection) {
        return new SettlementExtractResponse(
                projection.getId(),
                projection.getAssignorId(),
                projection.getAssignorName(),
                projection.getAssignorDocumentNumber(),
                projection.getSettlementDateTime(),
                projection.getValuationDate(),
                projection.getTargetCurrencyCode(),
                projection.getTotalFaceValue(),
                projection.getTotalDiscountAmount(),
                projection.getTotalNetAmount()
        );
    }
}
