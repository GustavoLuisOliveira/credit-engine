package com.credit.engine.domain.shared.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    @DisplayName("Deve criar Money normalizando a escala decimal para 4 casas e convertendo o código da moeda para caixa alta")
    void shouldCreateMoneyWithNormalizedScaleAndUppercaseCurrency() {
        Money money = Money.of(new BigDecimal("100.5"), "brl");

        assertThat(money.getAmount()).isEqualByComparingTo("100.5000");
        assertThat(money.getCurrencyCode()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("Deve rejeitar valor monetário negativo lançando IllegalArgumentException")
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-1"), "BRL"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve rejeitar código de moeda inválido que não possua exatamente 3 caracteres ISO")
    void shouldRejectInvalidCurrencyCode() {
        assertThatThrownBy(() -> Money.of(BigDecimal.TEN, "US"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 letras");
    }

    @Test
    @DisplayName("Deve rejeitar operações aritméticas entre moedas de tipos diferentes")
    void shouldRejectOperationsBetweenDifferentCurrencies() {
        Money brl = Money.of(BigDecimal.TEN, "BRL");
        Money usd = Money.of(BigDecimal.TEN, "USD");

        assertThatThrownBy(() -> brl.add(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("diferentes");
    }

    @Test
    @DisplayName("Deve somar valores monetários pertencentes à mesma moeda com sucesso")
    void shouldAddValuesInSameCurrency() {
        Money a = Money.of(new BigDecimal("10.00"), "BRL");
        Money b = Money.of(new BigDecimal("5.50"), "BRL");

        assertThat(a.add(b).getAmount()).isEqualByComparingTo("15.5000");
    }

}