package com.credit.engine.domain.model.pricing;

import com.credit.engine.domain.model.receivable.ReceivableType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingParameterTest {

    @Test
    @DisplayName("Deve criar parâmetro de precificação e calcular a taxa total (Base + Spread) corretamente")
    void shouldCreateParameterAndComputeTotalRate() {
        // Dado um parâmetro de precificação válido para duplicata comercial com taxa base de 10% e spread de 1,5%
        PricingParameter parameter = PricingParameter.create(
                ReceivableType.COMMERCIAL_INVOICE, new BigDecimal("0.10"), new BigDecimal("0.015"), LocalDate.now()
        );

        // Deve calcular a taxa total como 0.115 (11,5%)
        assertThat(parameter.totalRate()).isEqualByComparingTo("0.115");
    }

    @Test
    @DisplayName("Deve rejeitar a criação de parâmetro de precificação com taxa base negativa")
    void shouldRejectNegativeBaseRate() {
        // Tentar criar um parâmetro com taxa base negativa (-0.01) deve lançar IllegalArgumentException
        assertThatThrownBy(() -> PricingParameter.create(
                ReceivableType.COMMERCIAL_INVOICE, new BigDecimal("-0.01"), new BigDecimal("0.015"), LocalDate.now())
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve rejeitar a criação de parâmetro de precificação com taxa de spread negativa")
    void shouldRejectNegativeSpreadRate() {
        // Tentar criar um parâmetro com taxa de spread negativa (-0.01) deve lançar IllegalArgumentException
        assertThatThrownBy(() -> PricingParameter.create(
                ReceivableType.COMMERCIAL_INVOICE, new BigDecimal("0.10"), new BigDecimal("-0.01"), LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve rejeitar a criação de parâmetro de precificação quando o tipo de recebível for nulo")
    void shouldRejectNullReceivableType() {
        // Tentar criar um parâmetro com tipo de recebível nulo deve lançar NullPointerException
        assertThatThrownBy(() -> PricingParameter.create(
                null, new BigDecimal("0.10"), new BigDecimal("0.015"), LocalDate.now()))
                .isInstanceOf(NullPointerException.class);
    }

}