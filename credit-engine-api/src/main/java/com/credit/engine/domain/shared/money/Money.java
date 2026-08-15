package com.credit.engine.domain.shared.money;

import com.credit.engine.domain.shared.currency.CurrencyCodeValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object monetário compartilhado por Receivable, Settlement e SettlementItem.
 * Encapsula BigDecimal + código de moeda (ISO 4217).

 * Referencia a moeda pelo código (String), não pelo objeto de domínio Currency
 */
public final class Money {

    private static final int SCALE = 4;

    private final BigDecimal amount;
    private final String currencyCode;

    private Money(BigDecimal amount, String currencyCode) {
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        Objects.requireNonNull(amount, "amount é obrigatório");
        String normalizedCode = CurrencyCodeValidator.validateAndNormalize(currencyCode);
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("O valor monetário não pode ser negativo");

        return new Money(amount.setScale(SCALE, RoundingMode.HALF_EVEN), normalizedCode);
    }

    public static Money zero(String currencyCode) {
        return of(BigDecimal.ZERO, currencyCode);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return Money.of(this.amount.add(other.amount), this.currencyCode);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return Money.of(this.amount.subtract(other.amount), this.currencyCode);
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isSameCurrency(Money other) {
        return other != null && this.currencyCode.equals(other.currencyCode);
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "Não é possível operar com um valor monetário nulo");
        if (!isSameCurrency(other)) {
            throw new IllegalArgumentException(
                    "Não é possível operar valores em moedas diferentes: " + this.currencyCode + " e " + other.currencyCode
            );
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0 && currencyCode.equals(money.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currencyCode);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currencyCode;
    }

}
