package com.credit.engine.infrastructure.persistence.repository.currency;

import com.credit.engine.infrastructure.persistence.entity.currency.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, UUID> {

    /**
     * Cotação mais recente para um par de moedas
     * apoiada pelo índice idx_exchange_rate_pair_date criado na migration V2.
     */
    Optional<ExchangeRateEntity> findFirstByOriginCurrencyIdAndDestinationCurrencyIdOrderByRateDateTimeDesc(
            String originCurrencyId, String destinationCurrencyId
    );
}
