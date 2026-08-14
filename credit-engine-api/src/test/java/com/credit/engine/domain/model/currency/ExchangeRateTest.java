package com.credit.engine.domain.model.currency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateTest {

    @Test
    @DisplayName("Deve converter o montante corretamente utilizando a taxa de câmbio informada")
    void shouldConvertAmountUsingRate() {
        // Criação de uma taxa de câmbio USD -> BRL com valor de 5.20
        ExchangeRate rate = ExchangeRate.create("USD", "BRL", new BigDecimal("5.20"), OffsetDateTime.now());

        // Execução da conversão para o montante de 100 USD
        BigDecimal convertedAmount = rate.convert(new BigDecimal("100"));

        // Validação de que 100 * 5.20 resulta em 520.00
        assertThat(convertedAmount).isEqualByComparingTo("520.00");
    }

    @Test
    @DisplayName("Deve rejeitar a criação de taxa de câmbio com valor igual a zero ou negativo")
    void shouldRejectNonPositiveRate() {
        assertThatThrownBy(() ->
                ExchangeRate.create("USD", "BRL", BigDecimal.ZERO, OffsetDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positiva");
    }

    @Test
    @DisplayName("Deve rejeitar a criação de taxa de câmbio quando a moeda de origem e destino forem iguais")
    void shouldRejectSameOriginAndDestinationCurrency() {
        assertThatThrownBy(() ->
                ExchangeRate.create("BRL", "BRL", new BigDecimal("1"), OffsetDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("diferentes");
    }

}