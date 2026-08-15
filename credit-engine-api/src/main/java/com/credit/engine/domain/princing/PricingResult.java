package com.credit.engine.domain.princing;

import com.credit.engine.domain.shared.money.Money;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Resultado imutável do cálculo de precificação de um Receivable.
 * Carrega exatamente os campos que o SettlementItem vai "fotografar" para auditoria imutável:
 * baseRate, spreadRate, term, discountAmount, presentValue.
 */
public final class PricingResult {

    private final BigDecimal baseRate;
    private final BigDecimal spreadRate;
    private final BigDecimal term;
    private final Money discountAmount;
    private final Money presentValue;

    public PricingResult(BigDecimal baseRate, BigDecimal spreadRate, BigDecimal term,
                         Money discountAmount, Money presentValue) {
        this.baseRate = Objects.requireNonNull(baseRate, "baseRate é obrigatório");
        this.spreadRate = Objects.requireNonNull(spreadRate, "spreadRate é obrigatório");
        this.term = Objects.requireNonNull(term, "term é obrigatório");
        this.discountAmount = Objects.requireNonNull(discountAmount, "discountAmount é obrigatório");
        this.presentValue = Objects.requireNonNull(presentValue, "presentValue é obrigatório");
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public BigDecimal getSpreadRate() {
        return spreadRate;
    }

    public BigDecimal getTerm() {
        return term;
    }

    public Money getDiscountAmount() {
        return discountAmount;
    }

    public Money getPresentValue() {
        return presentValue;
    }
}
