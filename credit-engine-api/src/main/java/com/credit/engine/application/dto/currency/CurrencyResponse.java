package com.credit.engine.application.dto.currency;

import com.credit.engine.domain.model.currency.Currency;

import java.time.Instant;

public record CurrencyResponse(
        String code,
        String name,
        String symbol,
        Instant createdAt,
        Instant updatedAt
) {

    public static CurrencyResponse toResponse(Currency domain) {
        return new CurrencyResponse(
                domain.getCode(),
                domain.getName(),
                domain.getSymbol(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

}
