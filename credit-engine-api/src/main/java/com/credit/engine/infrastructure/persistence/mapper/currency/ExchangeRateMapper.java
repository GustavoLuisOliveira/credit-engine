package com.credit.engine.infrastructure.persistence.mapper.currency;

import com.credit.engine.domain.model.currency.ExchangeRate;
import com.credit.engine.infrastructure.persistence.entity.currency.ExchangeRateEntity;
import org.springframework.stereotype.Component;

/**
 * Traduz entre o modelo de domínio e a entidade JPA
 */
@Component
public class ExchangeRateMapper {

    public ExchangeRateEntity toEntity(ExchangeRate domain) {
        return new ExchangeRateEntity(
                domain.getOriginCurrencyCode(),
                domain.getDestinationCurrencyCode(),
                domain.getRate(),
                domain.getRateDateTime()
        );
    }

    public ExchangeRate toDomain(ExchangeRateEntity entity) {
        return ExchangeRate.restore(
                entity.getId(),
                entity.getOriginCurrencyId(),
                entity.getDestinationCurrencyId(),
                entity.getRate(),
                entity.getRateDateTime(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
