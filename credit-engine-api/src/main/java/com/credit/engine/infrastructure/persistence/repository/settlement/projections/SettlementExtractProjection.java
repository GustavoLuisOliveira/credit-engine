package com.credit.engine.infrastructure.persistence.repository.settlement.projections;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Projection para resultado da query nativa de extrato de liquidação.
 */
public interface SettlementExtractProjection {

    UUID getId();

    UUID getAssignorId();

    String getAssignorName();

    String getAssignorDocumentNumber();

    Instant getSettlementDateTime();

    LocalDate getValuationDate();

    String getTargetCurrencyCode();

    BigDecimal getTotalFaceValue();

    BigDecimal getTotalDiscountAmount();

    BigDecimal getTotalNetAmount();

}
