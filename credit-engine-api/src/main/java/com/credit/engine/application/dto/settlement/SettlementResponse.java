package com.credit.engine.application.dto.settlement;

import com.credit.engine.domain.model.settlement.Settlement;
import com.credit.engine.domain.model.settlement.SettlementItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        UUID assignorId,
        Instant settlementDateTime,
        LocalDate valuationDate,
        String targetCurrencyCode,
        BigDecimal totalFaceValue,
        BigDecimal totalDiscountAmount,
        BigDecimal totalNetAmount,
        List<SettlementItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
    public static SettlementResponse toResponse(Settlement domain, List<SettlementItem> items) {
        return new SettlementResponse(
                domain.getId(),
                domain.getAssignorId(),
                domain.getSettlementDateTime(),
                domain.getValuationDate(),
                domain.getTargetCurrencyCode(),
                domain.getTotalFaceValue().getAmount(),
                domain.getTotalDiscountAmount().getAmount(),
                domain.getTotalNetAmount().getAmount(),
                items.stream().map(SettlementItemResponse::toResponse).toList(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
