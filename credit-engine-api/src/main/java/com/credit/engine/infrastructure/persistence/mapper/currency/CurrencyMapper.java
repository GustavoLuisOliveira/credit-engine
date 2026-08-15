package com.credit.engine.infrastructure.persistence.mapper.currency;

import com.credit.engine.domain.model.currency.Currency;
import com.credit.engine.infrastructure.persistence.entity.currency.CurrencyEntity;
import org.springframework.stereotype.Component;

/**
 * Traduz entre o modelo de domínio e a entidade JPA
 */
@Component
public class CurrencyMapper {

    public CurrencyEntity toEntity(Currency domain) {
        return new CurrencyEntity(
                domain.getCode(), domain.getName(), domain.getSymbol()
        );
    }

    public Currency toDomain(CurrencyEntity entity) {
        return Currency.restore(
                entity.getId(),
                entity.getName(),
                entity.getSymbol(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
