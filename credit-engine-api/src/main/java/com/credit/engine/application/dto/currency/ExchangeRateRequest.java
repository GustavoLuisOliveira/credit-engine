package com.credit.engine.application.dto.currency;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExchangeRateRequest(
        @NotBlank(message = "O código da moeda de origem é obrigatório.")
        @Size(min = 3, max = 3, message = "O código da moeda de origem deve conter exatamente 3 letras (ex: USD).")
        String originCurrencyCode,

        @NotBlank(message = "O código da moeda de destino é obrigatório.")
        @Size(min = 3, max = 3, message = "O código da moeda de destino deve conter exatamente 3 letras (ex: BRL).")
        String destinationCurrencyCode,

        @NotNull(message = "A taxa de câmbio é obrigatória.")
        @DecimalMin(value = "0.0", inclusive = false, message = "A taxa de câmbio deve ser maior que zero.")
        BigDecimal rate,

        @NotNull(message = "A data/hora da cotação é obrigatória.")
        @PastOrPresent(message = "A data/hora da cotação não pode estar no futuro.")
        OffsetDateTime rateDateTime
) {
}
