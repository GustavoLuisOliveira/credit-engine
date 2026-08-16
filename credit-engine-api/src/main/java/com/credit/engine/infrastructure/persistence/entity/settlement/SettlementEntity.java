package com.credit.engine.infrastructure.persistence.entity.settlement;

import com.credit.engine.infrastructure.persistence.entity.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade JPA de Settlement. Mapeamento puro para a tabela `settlement`.
 * Assim como ReceivableEntity referencia a moeda por código (String), sem @ManyToOne
 * para CurrencyEntity/AssignorEntity, evitando acoplamento entre agregados via JPA.
 * Todas as colunas de negócio são updatable = false: fotografia de auditoria imutável.
 */
@Getter
@Entity
@Table(name = "settlement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementEntity extends BaseEntity {

    @Column(name = "assignor_id", nullable = false, updatable = false)
    private UUID assignorId;

    @Column(name = "settlement_date_time", nullable = false, updatable = false)
    private Instant settlementDateTime;

    @Column(name = "valuation_date", nullable = false, updatable = false)
    private LocalDate valuationDate;

    @Column(name = "target_currency_id", length = 3, nullable = false, updatable = false)
    private String targetCurrencyId;

    @Column(name = "total_face_value", nullable = false, precision = 18, scale = 4, updatable = false)
    private BigDecimal totalFaceValue;

    @Column(name = "total_discount_amount", nullable = false, precision = 18, scale = 4, updatable = false)
    private BigDecimal totalDiscountAmount;

    @Column(name = "total_net_amount", nullable = false, precision = 18, scale = 4, updatable = false)
    private BigDecimal totalNetAmount;


    public SettlementEntity(UUID assignorId, Instant settlementDateTime, LocalDate valuationDate, String targetCurrencyId,
                            BigDecimal totalFaceValue, BigDecimal totalDiscountAmount, BigDecimal totalNetAmount) {
        this.assignorId = assignorId;
        this.settlementDateTime = settlementDateTime;
        this.valuationDate = valuationDate;
        this.targetCurrencyId = targetCurrencyId;
        this.totalFaceValue = totalFaceValue;
        this.totalDiscountAmount = totalDiscountAmount;
        this.totalNetAmount = totalNetAmount;
    }

}
