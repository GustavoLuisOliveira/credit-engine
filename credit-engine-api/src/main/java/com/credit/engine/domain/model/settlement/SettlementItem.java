package com.credit.engine.domain.model.settlement;

import com.credit.engine.domain.shared.model.BaseDomainModel;
import com.credit.engine.domain.shared.money.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de domínio de SettlementItem, fotografia de auditoria imutável do resultado
 * de precificação de um único Receivable dentro de um lote de Settlement.
 *
 * faceValue, discountAmount e presentValue são sempre expressos na moeda ORIGINAL do título (originalCurrencyCode);
 * settlementAmount é sempre expresso na moeda de liquidação (settlementCurrencyCode) a conversão cambial,
 * quando houver, incide apenas sobre o presentValue já descontado, nunca sobre o faceValue bruto.
 *
 * baseRate/spreadRate gravam a FRAÇÃO decimal que efetivamente alimentou a fórmula de deságio (ex: 0.015000),
 * e não o formato percentual usado em pricing_parameter (ex: 1.500000):
 * é o valor realmente aplicado no cálculo que fica congelado aqui.
 *
 * term é o prazo em DIAS corridos entre a data de liquidação e o vencimento do título, fato de auditoria legível.
 * termMonths é o prazo fracionário em meses que efetivamente alimentou o expoente da
 * fórmula de deságio ((1 + totalRate) ^ termMonths).
 */
public class SettlementItem extends BaseDomainModel {

    private final UUID settlementId;
    private final UUID receivableId;
    private final int term;
    private final BigDecimal termMonths;
    private final BigDecimal baseRate;
    private final BigDecimal spreadRate;
    private final Money faceValue;
    private final Money discountAmount;
    private final Money presentValue;
    private final BigDecimal exchangeRateUsed;
    private final Money settlementAmount;


    private SettlementItem(UUID id, UUID settlementId, UUID receivableId, int term, BigDecimal termMonths, BigDecimal baseRate, BigDecimal spreadRate,
                           Money faceValue, Money discountAmount, Money presentValue,
                           BigDecimal exchangeRateUsed, Money settlementAmount, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.settlementId = Objects.requireNonNull(settlementId, "settlementId é obrigatório");
        this.receivableId = Objects.requireNonNull(receivableId, "receivableId é obrigatório");
        this.term = validatePositiveTermDays(term);
        this.termMonths = validatePositiveTermMonths(termMonths);
        this.baseRate = validateNonNegative(baseRate, "baseRate");
        this.spreadRate = validateNonNegative(spreadRate, "spreadRate");
        this.faceValue = validatePositiveMoney(faceValue, "faceValue");
        this.discountAmount = Objects.requireNonNull(discountAmount, "discountAmount é obrigatório");
        this.presentValue = validatePositiveMoney(presentValue, "presentValue");
        this.exchangeRateUsed = validatePositiveRate(exchangeRateUsed);
        this.settlementAmount = validatePositiveMoney(settlementAmount, "settlementAmount");
        requireCoherentCurrencies();
    }

    /** Cria um novo item de liquidação, resultado do cálculo de precificação de um Receivable. */
    public static SettlementItem create(UUID settlementId, UUID receivableId, int termDays, BigDecimal termMonths, BigDecimal baseRate, BigDecimal spreadRate,
                                        Money faceValue, Money discountAmount, Money presentValue,
                                        BigDecimal exchangeRateUsed, Money settlementAmount) {
        return new SettlementItem(
                null, settlementId, receivableId, termDays, termMonths, baseRate, spreadRate, faceValue,
                discountAmount, presentValue, exchangeRateUsed, settlementAmount, null, null
        );
    }

    /** Reidrata um item de liquidação já persistido. */
    public static SettlementItem restore(UUID id, UUID settlementId, UUID receivableId, int termDays, BigDecimal termMonths, BigDecimal baseRate, BigDecimal spreadRate,
                                         Money faceValue, Money discountAmount, Money presentValue,
                                         BigDecimal exchangeRateUsed, Money settlementAmount, Instant createdAt, Instant updatedAt) {
        return new SettlementItem(
                id, settlementId, receivableId, termDays, termMonths, baseRate, spreadRate, faceValue,
                discountAmount, presentValue, exchangeRateUsed, settlementAmount, createdAt, updatedAt
        );
    }

    /** Soma de base_rate + spread_rate, espelha a coluna gerada total_rate do banco (base_rate + spread_rate). */
    public BigDecimal totalRate() {
        return baseRate.add(spreadRate);
    }

    private static int validatePositiveTermDays(int termDays) {
        if (termDays <= 0)
            throw new IllegalArgumentException("termDays (prazo em dias) deve ser maior que zero");
        return termDays;
    }

    private static BigDecimal validatePositiveTermMonths(BigDecimal termMonths) {
        Objects.requireNonNull(termMonths, "termMonths é obrigatório");
        if (termMonths.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("termMonths (prazo fracionário usado na fórmula) deve ser maior que zero");
        return termMonths;
    }

    private static BigDecimal validateNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " é obrigatório");
        if (value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException(fieldName + " não pode ser negativo");
        return value;
    }

    private static BigDecimal validatePositiveRate(BigDecimal exchangeRateUsed) {
        Objects.requireNonNull(exchangeRateUsed, "exchangeRateUsed é obrigatório");
        if (exchangeRateUsed.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("exchangeRateUsed deve ser maior que zero");
        return exchangeRateUsed;
    }

    private static Money validatePositiveMoney(Money money, String fieldName) {
        Objects.requireNonNull(money, fieldName + " é obrigatório");
        if (!money.isPositive())
            throw new IllegalArgumentException(fieldName + " deve ser maior que zero");
        return money;
    }

    private void requireCoherentCurrencies() {
        if (!faceValue.isSameCurrency(discountAmount) || !faceValue.isSameCurrency(presentValue)) {
            throw new IllegalArgumentException("faceValue, discountAmount e presentValue devem estar todos na moeda original do título");
        }
    }

    public UUID getSettlementId() {
        return settlementId;
    }

    public UUID getReceivableId() {
        return receivableId;
    }

    public int getTerm() {
        return term;
    }

    public BigDecimal getTermMonths() {
        return termMonths;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public BigDecimal getSpreadRate() {
        return spreadRate;
    }

    public Money getFaceValue() {
        return faceValue;
    }

    public Money getDiscountAmount() {
        return discountAmount;
    }

    public Money getPresentValue() {
        return presentValue;
    }

    public BigDecimal getExchangeRateUsed() {
        return exchangeRateUsed;
    }

    public Money getSettlementAmount() {
        return settlementAmount;
    }

}
