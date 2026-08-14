package com.credit.engine.application.dto.currency;

import com.credit.engine.domain.model.currency.Currency;

import java.time.OffsetDateTime;

public record CurrencyResponse(
        String code,
        String name,
        String symbol,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
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
