package com.credit.engine.domain.princing.strategy;

import com.credit.engine.domain.model.receivable.Receivable;
import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.domain.princing.PricingResult;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Strategy Pattern:
 * cada tipo de recebível pode ter uma regra de precificação diferente.
 * A taxa base e o spread NÃO são resolvidos aqui, chegam já resolvidos (consultados na tabela pricing_parameter).
 */
public interface PricingStrategy {

    ReceivableType supports();

    PricingResult calculate(Receivable receivable, BigDecimal baseRateFraction, BigDecimal spreadRateFraction, LocalDate valuationDate);

}
