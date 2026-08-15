package com.credit.engine.domain.model.currency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyTest {

    @Test
    @DisplayName("Deve criar a moeda convertendo o código ISO para caixa alta (uppercase)")
    void shouldCreateCurrencyWithUppercaseCode() {
        Currency domain = Currency.create("brl", "Real Brasileiro", "R$");

        assertThat(domain.getCode()).isEqualTo("BRL");
        assertThat(domain.getName()).isEqualTo("Real Brasileiro");
    }

    @Test
    @DisplayName("Deve rejeitar código de moeda que não contenha exatamente 3 caracteres")
    void shouldRejectCodeWithWrongLength() {
        assertThatThrownBy(() -> Currency.create("br", "Real", "R$"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 letras");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar uma moeda com nome nulo")
    void shouldRejectNullName() {
        assertThatThrownBy(() -> Currency.create("brl", null, "R$"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar uma moeda com símbolo nulo")
    void shouldRejectNullSymbol() {
        assertThatThrownBy(() -> Currency.create("brl", "Real", null))
                .isInstanceOf(NullPointerException.class);
    }

}