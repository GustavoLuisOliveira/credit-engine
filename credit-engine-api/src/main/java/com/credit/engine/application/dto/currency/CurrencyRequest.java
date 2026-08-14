package com.credit.engine.application.dto.currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CurrencyRequest(
        @NotBlank(message = "O código da moeda é obrigatório.")
        @Size(min = 3, max = 3, message = "O código da moeda deve possuir exatamente 3 letras (ex: BRL, USD).")
        String code,

        @NotBlank(message = "O nome da moeda é obrigatório.")
        @Size(max = 50, message = "O nome da moeda deve conter no máximo 50 caracteres.")
        String name,

        @NotBlank(message = "O símbolo da moeda é obrigatório.")
        @Size(max = 5, message = "O símbolo da moeda deve conter no máximo 5 caracteres.")
        String symbol
) {
}
