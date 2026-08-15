package com.credit.engine.application.dto.receivable;

import com.credit.engine.domain.model.receivable.Receivable;
import com.credit.engine.domain.model.receivable.ReceivableStatus;
import com.credit.engine.domain.model.receivable.ReceivableType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReceivableResponse(
        UUID id,
        UUID assignorId,
        ReceivableType type,
        String documentNumber,
        BigDecimal faceValue,
        String currencyCode,
        LocalDate dueDate,
        ReceivableStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReceivableResponse toResponse(Receivable domain) {
        return new ReceivableResponse(
                domain.getId(),
                domain.getAssignorId(),
                domain.getType(),
                domain.getDocumentNumber(),
                domain.getFaceValue().getAmount(),
                domain.getFaceValue().getCurrencyCode(),
                domain.getDueDate(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
