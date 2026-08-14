package com.credit.engine.application.dto.currency;

import com.credit.engine.domain.model.currency.ExchangeRate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExchangeRateResponse(
        UUID id,
        String originCurrencyCode,
        String destinationCurrencyCode,
        BigDecimal rate,
        Instant rateDateTime,
        Instant createdAt,
        Instant updatedAt
) {
    public static ExchangeRateResponse toResponse(ExchangeRate domain) {
        return new ExchangeRateResponse(
                domain.getId(),
                domain.getOriginCurrencyCode(),
                domain.getDestinationCurrencyCode(),
                domain.getRate(),
                domain.getRateDateTime(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
