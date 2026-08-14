package com.credit.engine.application.dto.currency;

import com.credit.engine.domain.model.currency.ExchangeRate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExchangeRateResponse(
        UUID id,
        String originCurrencyCode,
        String destinationCurrencyCode,
        BigDecimal rate,
        OffsetDateTime rateDateTime,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
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
