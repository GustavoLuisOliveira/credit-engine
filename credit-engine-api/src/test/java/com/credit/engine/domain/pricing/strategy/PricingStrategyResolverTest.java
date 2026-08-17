package com.credit.engine.domain.pricing.strategy;

import com.credit.engine.domain.model.receivable.ReceivableType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingStrategyResolverTest {

    private final PricingStrategyResolver resolver = new PricingStrategyResolver(
            List.of(new CommercialInvoicePricingStrategy(), new PostDatedCheckPricingStrategy()));

    @Test
    @DisplayName("Deve resolver a estratégia correta de precificação de acordo com o tipo do recebível")
    void shouldResolveStrategyByType() {
        // Verifica se o tipo COMMERCIAL_INVOICE resolve para CommercialInvoicePricingStrategy
        assertThat(resolver.resolve(ReceivableType.COMMERCIAL_INVOICE))
                .isInstanceOf(CommercialInvoicePricingStrategy.class);

        // Verifica se o tipo POST_DATED_CHECK resolve para PostDatedCheckPricingStrategy
        assertThat(resolver.resolve(ReceivableType.POST_DATED_CHECK))
                .isInstanceOf(PostDatedCheckPricingStrategy.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando tentar resolver um tipo de recebível sem estratégia registrada")
    void shouldThrowWhenStrategyNotRegistered() {
        // Dado um resolver sem nenhuma estratégia registrada
        PricingStrategyResolver emptyResolver = new PricingStrategyResolver(List.of());

        // Tentar resolver qualquer tipo deve lançar IllegalStateException
        assertThatThrownBy(() -> emptyResolver.resolve(ReceivableType.COMMERCIAL_INVOICE))
                .isInstanceOf(IllegalStateException.class);
    }
}