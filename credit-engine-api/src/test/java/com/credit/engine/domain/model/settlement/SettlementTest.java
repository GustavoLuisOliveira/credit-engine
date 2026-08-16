package com.credit.engine.domain.model.settlement;

import com.credit.engine.domain.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementTest {

    private static final UUID ASSIGNOR_ID = UUID.randomUUID();
    private static final LocalDate VALUATION_DATE = LocalDate.of(2026, 8, 16);

    @Test
    @DisplayName("Deve montar uma liquidação válida e derivar a moeda alvo a partir dos totais")
    void shouldCreateValidSettlementAndDeriveTargetCurrencyFromTotals() {
        Money totalFaceValue = Money.of(new BigDecimal("10000.00"), "BRL");
        Money totalDiscount = Money.of(new BigDecimal("150.00"), "BRL");
        Money totalNet = Money.of(new BigDecimal("9850.00"), "BRL");

        Settlement settlement = Settlement.create(ASSIGNOR_ID, Instant.now(), VALUATION_DATE, totalFaceValue, totalDiscount, totalNet);

        assertThat(settlement.getTargetCurrencyCode()).isEqualTo("BRL");
        assertThat(settlement.getValuationDate()).isEqualTo(VALUATION_DATE);
        assertThat(settlement.getTotalFaceValue()).isEqualTo(totalFaceValue);
        assertThat(settlement.getTotalDiscountAmount()).isEqualTo(totalDiscount);
        assertThat(settlement.getTotalNetAmount()).isEqualTo(totalNet);
    }

    @Test
    @DisplayName("Deve falhar ao criar liquidação quando os três totais não estiverem na mesma moeda")
    void shouldFailCreationWhenTotalsAreInDifferentCurrencies() {
        Money totalFaceValueBrl = Money.of(new BigDecimal("10000.00"), "BRL");
        Money totalDiscountUsd = Money.of(new BigDecimal("30.00"), "USD");
        Money totalNetBrl = Money.of(new BigDecimal("9850.00"), "BRL");

        assertThatThrownBy(() -> Settlement.create(ASSIGNOR_ID, Instant.now(),VALUATION_DATE, totalFaceValueBrl, totalDiscountUsd, totalNetBrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mesma moeda alvo");
    }

    @Test
    @DisplayName("Deve falhar ao criar liquidação quando o ID do cedente for nulo")
    void shouldFailCreationWhenAssignorIdIsNull() {
        Money zero = Money.zero("BRL");

        assertThatThrownBy(() -> Settlement.create(null, Instant.now(), VALUATION_DATE, zero, zero, zero))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Deve falhar ao criar liquidação quando a data/hora da liquidação for nula")
    void shouldFailCreationWhenSettlementDateTimeIsNull() {
        Money zero = Money.zero("BRL");

        assertThatThrownBy(() -> Settlement.create(ASSIGNOR_ID, null, VALUATION_DATE, zero, zero, zero))
                .isInstanceOf(NullPointerException.class);
    }
}