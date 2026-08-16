package com.credit.engine.domain.princing.strategy;

import com.credit.engine.domain.model.receivable.Receivable;
import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.domain.princing.PricingResult;
import com.credit.engine.domain.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class CommercialInvoicePricingStrategyTest {

    private final CommercialInvoicePricingStrategy strategy = new CommercialInvoicePricingStrategy();

    @Test
    @DisplayName("Deve suportar exclusivamente o tipo de recebível COMMERCIAL_INVOICE")
    void shouldSupportCommercialInvoiceType() {
        // Verifica se a estratégia declara suporte correto para duplicata comercial
        assertThat(strategy.supports()).isEqualTo(ReceivableType.COMMERCIAL_INVOICE);
    }

    @Test
    @DisplayName("Deve rejeitar prazo zero (vencimento na própria data de liquidação), pois o spread nunca seria aplicado")
    void shouldRejectSameDaySettlement() {
        // Dado um recebível vencendo no mesmo dia do cálculo de liquidação
        LocalDate dueDate = LocalDate.now();
        Receivable receivable = Receivable.create(
                UUID.randomUUID(), ReceivableType.COMMERCIAL_INVOICE, "NF-001",
                Money.of(new BigDecimal("10000.00"), "BRL"), dueDate
        );

        // Quando calcula a precificação com prazo zero
        // Então deve rejeitar: term=0 zera o fator de desconto ((1+totalRate)^0 = 1),
        // o que significa que o spread de risco nunca é efetivamente cobrado e a operação não teria lucro
        assertThatThrownBy(() -> strategy.calculate(
                receivable, new BigDecimal("0.10"), new BigDecimal("0.015"), dueDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no mínimo, 1 dia");
    }

    @Test
    @DisplayName("Deve aceitar e aplicar o spread no prazo mínimo permitido de 1 dia")
    void shouldApplyDiscountForMinimumOneDayTerm() {
        // Dado um recebível vencendo 1 dia após a data de liquidação (prazo mínimo válido)
        LocalDate valuationDate = LocalDate.now();
        LocalDate dueDate = valuationDate.plusDays(1);
        Receivable receivable = Receivable.create(
                UUID.randomUUID(), ReceivableType.COMMERCIAL_INVOICE, "NF-004",
                Money.of(new BigDecimal("10000.00"), "BRL"), dueDate
        );

        // Quando calcula a precificação
        PricingResult result = strategy.calculate(
                receivable, new BigDecimal("0.10"), new BigDecimal("0.015"), valuationDate);

        // Então o prazo deve ser > 0 e o spread deve gerar algum desconto (ainda que pequeno)
        assertThat(result.getTerm()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getDiscountAmount().getAmount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getPresentValue().getAmount()).isLessThan(new BigDecimal("10000.0000"));
    }

    @Test
    @DisplayName("Deve aplicar o deságio/desconto composto corretamente para prazo de um mês")
    void shouldApplyDiscountForOneMonthTerm() {
        // Dado um recebível com vencimento para 30 dias após a data de liquidação
        LocalDate valuationDate = LocalDate.now();
        LocalDate dueDate = valuationDate.plusDays(30);
        Receivable receivable = Receivable.create(
                UUID.randomUUID(), ReceivableType.COMMERCIAL_INVOICE, "NF-002",
                Money.of(new BigDecimal("10000.00"), "BRL"), dueDate);

        // Quando calcula a precificação com taxa base 10% e spread 1,5% (total = 11,5%)
        PricingResult result = strategy.calculate(
                receivable, new BigDecimal("0.10"), new BigDecimal("0.015"), valuationDate);

        // Prazo = 30 dias / 30 = 1 mês; fator = (1.115)^1 = 1.115
        // VP = 10000 / 1.115 ≈ 8968.6099 ; Desconto ≈ 1031.3901
        assertThat(result.getTerm()).isEqualByComparingTo("1.0000000000");
        assertThat(result.getPresentValue().getAmount().doubleValue()).isCloseTo(8968.6099, within(0.01));
        assertThat(result.getDiscountAmount().getAmount().doubleValue()).isCloseTo(1031.3901, within(0.01));
    }

    @Test
    @DisplayName("Deve rejeitar o cálculo de precificação para recebível já vencido na data de liquidação")
    void shouldRejectAlreadyDueReceivable() {
        // Dado um recebível cuja data de vencimento é anterior à data de liquidação
        LocalDate valuationDate = LocalDate.now();
        LocalDate dueDate = valuationDate.minusDays(1);
        Receivable receivable = Receivable.create(
                UUID.randomUUID(), ReceivableType.COMMERCIAL_INVOICE, "NF-003",
                Money.of(new BigDecimal("1000.00"), "BRL"), dueDate
        );

        // Deve lançar IllegalArgumentException contendo a mensagem indicando que o título está vencido
        assertThatThrownBy(() -> strategy.calculate(
                receivable, new BigDecimal("0.10"), new BigDecimal("0.015"), valuationDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vencido");
    }

}