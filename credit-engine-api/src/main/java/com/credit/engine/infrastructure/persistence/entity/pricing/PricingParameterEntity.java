package com.credit.engine.infrastructure.persistence.entity.pricing;

import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.infrastructure.persistence.entity.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade JPA de PricingParameter. Mapeamento puro para a tabela `pricing_parameter`.
 * Todas as colunas são updatable = false: o registro é append-only, nunca sofre UPDATE (ver comentário na migration V5).
 */
@Getter
@Entity
@Table(name = "pricing_parameter")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PricingParameterEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "receivable_type", length = 50, nullable = false, updatable = false)
    private ReceivableType receivableType;

    @Column(name = "base_rate", nullable = false, precision = 9, scale = 6, updatable = false)
    private BigDecimal baseRate;

    @Column(name = "spread_rate", nullable = false, precision = 9, scale = 6, updatable = false)
    private BigDecimal spreadRate;

    @Column(name = "effective_date", nullable = false, updatable = false)
    private LocalDate effectiveDate;

    public PricingParameterEntity(ReceivableType receivableType, BigDecimal baseRate, BigDecimal spreadRate, LocalDate effectiveDate) {
        this.receivableType = receivableType;
        this.baseRate = baseRate;
        this.spreadRate = spreadRate;
        this.effectiveDate = effectiveDate;
    }

}
