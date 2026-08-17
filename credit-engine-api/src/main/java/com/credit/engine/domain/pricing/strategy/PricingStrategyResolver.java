package com.credit.engine.domain.pricing.strategy;

import com.credit.engine.domain.model.receivable.ReceivableType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry do Strategy Pattern:
 * seleciona a PricingStrategy correta a partir do ReceivableType, sem if/switch.
 * O Spring injeta automaticamente todos os @Component que implementam PricingStrategy.
 */
@Component
public class PricingStrategyResolver {

    private final Map<ReceivableType, PricingStrategy> strategiesByType;

    public PricingStrategyResolver(List<PricingStrategy> strategies) {
        this.strategiesByType = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(PricingStrategy::supports, Function.identity()));
    }

    public PricingStrategy resolve(ReceivableType type) {
        PricingStrategy strategy = strategiesByType.get(type);
        if (strategy == null)
            throw new IllegalStateException("Nenhuma PricingStrategy registrada para o tipo: " + type);

        return strategy;
    }

}
