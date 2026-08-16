package com.credit.engine.application.dto.settlement;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SettlementRequest(
        @NotNull(message = "Informe o cedente.")
        UUID assignorId,

        @NotNull(message = "A data de liquidação é obrigatória.")
        @FutureOrPresent(message = "A data de liquidação não pode ser retroativa.")
        LocalDate valuationDate,

        @NotNull(message = "A moeda alvo de pagamento é obrigatória.")
        @Size(min = 3, max = 3, message = "O código da moeda alvo deve possuir exatamente 3 letras (ex: BRL, USD).")
        String targetCurrencyCode,

        @NotEmpty(message = "Selecione pelo menos um recebível para liquidar.")
        List<UUID> receivableIds
) {
}
