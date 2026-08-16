package com.credit.engine.domain.princing.strategy;

import com.credit.engine.domain.model.receivable.Receivable;
import com.credit.engine.domain.princing.PricingResult;
import com.credit.engine.domain.shared.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Base comum das PricingStrategy concretas.
 * Centraliza a fórmula de deságio: Valor Presente = Valor Face / (1 + Taxa Base + Spread) ^ Prazo
 * As subclasses hoje diferem apenas pelo ReceivableType suportado (a taxa em si vem de fora, via pricing_parameter),
 * mas o Strategy Pattern preserva o ponto de extensão caso um tipo futuro precise de uma fórmula diferente.
 * *
 * O Prazo (term) é expresso em meses fracionários (dias corridos / 30), o que torna o expoente não inteiro.
 * BigDecimal não tem potenciação fracionária nativa, então o fator de desconto é calculado via double (Math.pow)
 * e o resultado final volta para BigDecimal, prática padrão de mercado para juros compostos com prazo fracionário.
 */
abstract class AbstractPricingStrategy implements PricingStrategy {

    private static final int MONEY_SCALE = 4;
    private static final int RATE_SCALE = 10;
    private static final BigDecimal DAYS_PER_MONTH = new BigDecimal("30");

    @Override
    public PricingResult calculate(Receivable receivable, BigDecimal baseRateFraction, BigDecimal spreadRateFraction, LocalDate valuationDate) {
        Objects.requireNonNull(receivable, "receivable é obrigatório");
        Objects.requireNonNull(baseRateFraction, "baseRate é obrigatório");
        Objects.requireNonNull(spreadRateFraction, "spreadRate é obrigatório");
        Objects.requireNonNull(valuationDate, "valuationDate é obrigatório");

        // Prazo fracionário em meses (ex: 45 dias corridos até o vencimento = 1.5 mês)
        BigDecimal term = calculateTermInMonths(valuationDate, receivable.getDueDate());
        // Taxa total do período = taxa base de mercado + spread de risco do tipo de recebível
        BigDecimal totalRate = baseRateFraction.add(spreadRateFraction);

        BigDecimal faceValueAmount = receivable.getFaceValue().getAmount();
        // Valor Presente = Valor Face / (1 + totalRate) ^ term
        BigDecimal presentValueAmount = calculatePresentValue(faceValueAmount, totalRate, term);

        // Valor presente sempre na mesma moeda do título original
        // (conversão cambial, se houver, é aplicada depois, na liquidação)
        Money presentValue = Money.of(presentValueAmount, receivable.getFaceValue().getCurrencyCode());
        // Deságio = diferença entre o valor de face e o valor presente calculado
        Money discountAmount = receivable.getFaceValue().subtract(presentValue);

        return new PricingResult(baseRateFraction, spreadRateFraction, term, discountAmount, presentValue);
    }

    private BigDecimal calculateTermInMonths(LocalDate valuationDate, LocalDate dueDate) {
        long days = ChronoUnit.DAYS.between(valuationDate, dueDate);

        // Prazo mínimo de 1 dia: cobre tanto o recebível já vencido (days < 0) quanto o
        // vencimento na própria data de liquidação (days == 0). Em ambos os casos o fator
        // de desconto seria neutro (term=0 → (1+totalRate)^0 = 1), ou seja, o spread de
        // risco nunca chegaria a ser aplicado e a operação sairia sem lucro para a mesa.
        if (days < 1) {
            throw new IllegalArgumentException(
                    "O prazo entre a liquidação e o vencimento deve ser de, no mínimo, 1 dia. "
                            + "Não é possível aplicar o spread de risco a um recebível já vencido ou "
                            + "vencendo na própria data de liquidação (dueDate=" + dueDate
                            + ", valuationDate=" + valuationDate + ")");
        }

        return BigDecimal.valueOf(days).divide(DAYS_PER_MONTH, RATE_SCALE, RoundingMode.HALF_EVEN);
    }

    private BigDecimal calculatePresentValue(BigDecimal faceValue, BigDecimal totalRate, BigDecimal term) {
        // Calcula o fator de desconto composto: (1 + Taxa Total) ^ Prazo
        // Utiliza Math.pow para suportar expoentes fracionários/prazos contínuos
        double discountFactor = Math.pow(1 + totalRate.doubleValue(), term.doubleValue());

        // Divide o valor nominal pelo fator de desconto acumulado
        // Aplica a escala definida para moeda
        return faceValue.divide(BigDecimal.valueOf(discountFactor), MONEY_SCALE, RoundingMode.HALF_EVEN);
    }

}
