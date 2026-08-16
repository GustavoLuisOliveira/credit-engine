package com.credit.engine.infrastructure.persistence.entity.settlement;

import com.credit.engine.infrastructure.persistence.entity.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade JPA de SettlementItem. Mapeamento puro para a tabela `settlement_item`.
 * Todas as colunas de negócio são updatable = false: fotografia de auditoria imutável.
 */
@Getter
@Entity
@Table(
        name = "settlement_item",
        uniqueConstraints = @UniqueConstraint(name = "uq_settlement_item_receivable", columnNames = "receivable_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementItemEntity extends BaseEntity {

    @Column(name = "settlement_id", nullable = false, updatable = false)
    private UUID settlementId;

    @Column(name = "receivable_id", nullable = false, updatable = false)
    private UUID receivableId;

    @Column(name = "term", nullable = false, updatable = false)
    private Integer term;

    @Column(name = "term_months", nullable = false, precision = 10, scale = 6, updatable = false)
    private BigDecimal termMonths;

    @Column(name = "base_rate", nullable = false, precision = 10, scale = 6, updatable = false)
    private BigDecimal baseRate;

    @Column(name = "spread_rate", nullable = false, precision = 10, scale = 6, updatable = false)
    private BigDecimal spreadRate;

    @Column(name = "original_currency_id", length = 3, nullable = false, updatable = false)
    private String originalCurrencyId;

    @Column(name = "face_value", nullable = false, precision = 18, scale = 4, updatable = false)
    private BigDecimal faceValue;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 4, updatable = false)
    private BigDecimal discountAmount;

    @Column(name = "present_value", nullable = false, precision = 18, scale = 4, updatable = false)
    private BigDecimal presentValue;

    @Column(name = "settlement_currency_id", length = 3, nullable = false, updatable = false)
    private String settlementCurrencyId;

    @Column(name = "exchange_rate_used", nullable = false, precision = 18, scale = 8, updatable = false)
    private BigDecimal exchangeRateUsed;

    @Column(name = "settlement_amount", nullable = false, precision = 18, scale = 4, updatable = false)
    private BigDecimal settlementAmount;

    public SettlementItemEntity(UUID settlementId, UUID receivableId, Integer term, BigDecimal termMonths, BigDecimal baseRate, BigDecimal spreadRate,
                                String originalCurrencyId, BigDecimal faceValue, BigDecimal discountAmount, BigDecimal presentValue,
                                String settlementCurrencyId, BigDecimal exchangeRateUsed, BigDecimal settlementAmount) {
        this.settlementId = settlementId;
        this.receivableId = receivableId;
        this.term = term;
        this.termMonths = termMonths;
        this.baseRate = baseRate;
        this.spreadRate = spreadRate;
        this.originalCurrencyId = originalCurrencyId;
        this.faceValue = faceValue;
        this.discountAmount = discountAmount;
        this.presentValue = presentValue;
        this.settlementCurrencyId = settlementCurrencyId;
        this.exchangeRateUsed = exchangeRateUsed;
        this.settlementAmount = settlementAmount;
    }

}
