package com.credit.engine.application.dto.pricing;

import com.credit.engine.domain.model.receivable.ReceivableType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PricingParameterRequest(
        @NotNull(message = "O tipo de recebível é obrigatório")
        ReceivableType receivableType,

        @NotNull(message = "A taxa base é obrigatória")
        @DecimalMin(value = "0.0", message = "A taxa base não pode ser negativa")
        @DecimalMax(value = "100.0", message = "taxa base não pode ser maior que 100%")
        BigDecimal baseRate,

        @NotNull(message = "A taxa de spread é obrigatória")
        @DecimalMin(value = "0.0", message = "A taxa de spread não pode ser negativa")
        @DecimalMax(value = "100.0", message = "A taxa de spread não pode ser maior que 100%")
        BigDecimal spreadRate,

        @NotNull(message = "A data de vigência é obrigatória")
        LocalDate effectiveDate
) {
}
