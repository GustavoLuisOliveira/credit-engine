package com.credit.engine.application.dto.receivable;

import com.credit.engine.domain.model.receivable.ReceivableType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReceivableRequest(
        @NotNull(message = "O identificador do cedente é obrigatório.")
        UUID assignorId,

        @NotNull(message = "O tipo do título é obrigatório.")
        ReceivableType type,

        @NotBlank(message = "O número do documento é obrigatório.")
        @Size(max = 50, message = "O número do documento deve conter no máximo 50 caracteres.")
        String documentNumber,

        @NotNull(message = "O valor de face é obrigatório.")
        @DecimalMin(value = "0.0", inclusive = false, message = "O valor de face deve ser maior que zero.")
        @Digits(integer = 14, fraction = 4, message = "O valor de face deve conter no máximo 14 dígitos inteiros e 4 casas decimais.")
        BigDecimal faceValue,

        @NotBlank(message = "O código da moeda é obrigatório.")
        @Size(min = 3, max = 3, message = "O código da moeda deve possuir exatamente 3 letras (ex: BRL, USD).")
        String currencyCode,

        @NotNull(message = "A data de vencimento é obrigatória.")
        @FutureOrPresent(message = "A data de vencimento não pode estar no passado.")
        LocalDate dueDate
) {
}
