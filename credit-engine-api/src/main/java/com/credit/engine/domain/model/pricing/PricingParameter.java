package com.credit.engine.domain.model.pricing;

import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.domain.shared.model.BaseDomainModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de domínio de PricingParameter
 * Representa a taxa base e o spread vigentes para um ReceivableType numa data de vigência.
 * Nunca é mutado após criado (append-only): uma mudança de taxa gera um novo
 * PricingParameter, preservando o histórico para auditoria.
 */
public class PricingParameter extends BaseDomainModel {

    private final ReceivableType receivableType;
    private final BigDecimal baseRate;
    private final BigDecimal spreadRate;
    private final LocalDate effectiveDate;

    private PricingParameter(UUID id, ReceivableType receivableType, BigDecimal baseRate, BigDecimal spreadRate, LocalDate effectiveDate, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.receivableType = Objects.requireNonNull(receivableType, "receivableType é obrigatório");
        this.baseRate = validateNonNegative(baseRate, "baseRate");
        this.spreadRate = validateNonNegative(spreadRate, "spreadRate");
        this.effectiveDate = Objects.requireNonNull(effectiveDate, "effectiveDate é obrigatório");
    }

    public static PricingParameter create(ReceivableType receivableType, BigDecimal baseRate, BigDecimal spreadRate, LocalDate effectiveDate) {
        return new PricingParameter(null, receivableType, baseRate, spreadRate, effectiveDate, null, null);
    }

    public static PricingParameter restore(UUID id, ReceivableType receivableType, BigDecimal baseRate, BigDecimal spreadRate, LocalDate effectiveDate, Instant createdAt, Instant updatedAt) {
        return new PricingParameter(id, receivableType, baseRate, spreadRate, effectiveDate, createdAt, updatedAt);
    }

    private static BigDecimal validateNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " é obrigatório");
        if (value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException(fieldName + " não pode ser negativo");

        return value;
    }

    /** Taxa base convertida de percentual (ex: 2.50) para fração decimal (0.025000), pronta para uso na fórmula de deságio. */
    public BigDecimal baseRateAsFraction() {
        return baseRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_EVEN);
    }

    /** Spread convertido de percentual (ex: 1.50) para fração decimal (0.015000), pronto para uso na fórmula de deságio. */
    public BigDecimal spreadRateAsFraction() {
        return spreadRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_EVEN);
    }

    /** Soma da taxa base com o spread, a taxa efetiva usada no expoente da fórmula de deságio. */
    public BigDecimal totalRate() {
        return baseRate.add(spreadRate);
    }

    public ReceivableType getReceivableType() {
        return receivableType;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public BigDecimal getSpreadRate() {
        return spreadRate;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }
}
