package com.credit.engine.infrastructure.persistence.entity.currency;

import com.credit.engine.infrastructure.persistence.entity.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidade JPA de ExchangeRate. Mapeamento puro para a tabela `exchange_rate`.
 */
@Getter
@Entity
@Table(name = "exchange_rate")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRateEntity extends BaseEntity {

    @Column(name = "origin_currency_id", length = 3, nullable = false)
    private String originCurrencyId;

    @Column(name = "destination_currency_id", length = 3, nullable = false)
    private String destinationCurrencyId;

    @Column(name = "rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal rate;

    @Column(name = "rate_date_time", nullable = false)
    private Instant rateDateTime;

    public ExchangeRateEntity(String originCurrencyId, String destinationCurrencyId, BigDecimal rate, Instant rateDateTime) {
        this.originCurrencyId = originCurrencyId;
        this.destinationCurrencyId = destinationCurrencyId;
        this.rate = rate;
        this.rateDateTime = rateDateTime;
    }

}
