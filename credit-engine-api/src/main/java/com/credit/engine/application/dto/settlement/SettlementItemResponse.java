package com.credit.engine.application.dto.settlement;

import com.credit.engine.domain.model.settlement.SettlementItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementItemResponse(
        UUID id,
        UUID settlementId,
        UUID receivableId,
        int term,
        BigDecimal termMonths,
        BigDecimal baseRate,
        BigDecimal spreadRate,
        BigDecimal totalRate,
        String originalCurrencyCode,
        BigDecimal faceValue,
        BigDecimal discountAmount,
        BigDecimal presentValue,
        String settlementCurrencyCode,
        BigDecimal exchangeRateUsed,
        BigDecimal settlementAmount,
        Instant createdAt,
        Instant updatedAt
) {
    public static SettlementItemResponse toResponse(SettlementItem domain) {
        return new SettlementItemResponse(
                domain.getId(),
                domain.getSettlementId(),
                domain.getReceivableId(),
                domain.getTerm(),
                domain.getTermMonths(),
                domain.getBaseRate(),
                domain.getSpreadRate(),
                domain.totalRate(),
                domain.getFaceValue().getCurrencyCode(),
                domain.getFaceValue().getAmount(),
                domain.getDiscountAmount().getAmount(),
                domain.getPresentValue().getAmount(),
                domain.getSettlementAmount().getCurrencyCode(),
                domain.getExchangeRateUsed(),
                domain.getSettlementAmount().getAmount(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
