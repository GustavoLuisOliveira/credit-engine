package com.credit.engine.domain.model.settlement;

import com.credit.engine.domain.shared.model.BaseDomainModel;
import com.credit.engine.domain.shared.money.Money;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de domínio de Settlement (Cabeçalho de Liquidação).
 * Representa o lote consolidado de cessão de recebíveis executado na mesa de operação.
 * Imutável após criado: os totais são "fotografados" no momento da execução do lote
 * (mesma razão de imutabilidade do SettlementItem).
 *
 * valuationDate é a data-base que alimentou a fórmula de deságio de TODOS os itens do lote.
 *
 * totalFaceValue, totalDiscountAmount e totalNetAmount já chegam aqui prontos: é a soma
 * acumulada de todos os SettlementItem do lote, item a item, feita pelo
 * SettlementServiceImpl antes de criar este objeto, não há cálculo nem conversão
 * pendente depois. Os três são sempre expressos na targetCurrency, nunca na moeda
 * original dos títulos: um lote pode misturar recebíveis em BRL e USD, e só dá pra somar
 * os valores se todos estiverem convertidos para a mesma moeda alvo antes da soma (não
 * existe "10.000 BRL + 2.000 USD = 12.000").
 */
public class Settlement extends BaseDomainModel {

    private final UUID assignorId;
    private final Instant settlementDateTime;
    private final LocalDate valuationDate;
    private final Money totalFaceValue;
    private final Money totalDiscountAmount;
    private final Money totalNetAmount;

    private Settlement(UUID id, UUID assignorId, Instant settlementDateTime, LocalDate valuationDate, Money totalFaceValue, Money totalDiscountAmount, Money totalNetAmount, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.assignorId = Objects.requireNonNull(assignorId, "assignorId é obrigatório");
        this.settlementDateTime = Objects.requireNonNull(settlementDateTime, "settlementDateTime é obrigatório");
        this.valuationDate = Objects.requireNonNull(valuationDate, "valuationDate é obrigatório");
        this.totalFaceValue = Objects.requireNonNull(totalFaceValue, "totalFaceValue é obrigatório");
        this.totalDiscountAmount = Objects.requireNonNull(totalDiscountAmount, "totalDiscountAmount é obrigatório");
        this.totalNetAmount = Objects.requireNonNull(totalNetAmount, "totalNetAmount é obrigatório");
        requireSameCurrency(this.totalFaceValue, this.totalDiscountAmount, this.totalNetAmount);
    }

    /** Cria um novo cabeçalho de liquidação, a partir dos totais já consolidados pela camada de aplicação. */
    public static Settlement create(UUID assignorId, Instant settlementDateTime, LocalDate valuationDate, Money totalFaceValue, Money totalDiscountAmount, Money totalNetAmount) {
        return new Settlement(null, assignorId, settlementDateTime, valuationDate, totalFaceValue, totalDiscountAmount, totalNetAmount, null, null);
    }

    /** Reidrata um cabeçalho de liquidação já persistido. */
    public static Settlement restore(UUID id, UUID assignorId, Instant settlementDateTime, LocalDate valuationDate, Money totalFaceValue, Money totalDiscountAmount, Money totalNetAmount, Instant createdAt, Instant updatedAt) {
        return new Settlement(id, assignorId, settlementDateTime, valuationDate, totalFaceValue, totalDiscountAmount, totalNetAmount, createdAt, updatedAt);
    }

    private static void requireSameCurrency(Money totalFaceValue, Money totalDiscountAmount, Money totalNetAmount) {
        if (!totalFaceValue.isSameCurrency(totalDiscountAmount) || !totalFaceValue.isSameCurrency(totalNetAmount))
            throw new IllegalArgumentException("Os totais do lote de liquidação devem estar todos na mesma moeda alvo (target currency)");
    }

    /** Moeda alvo do lote (escolhida pelo cedente para receber o valor consolidado). Derivada dos totais, evita duplicar o dado. */
    public String getTargetCurrencyCode() {
        return totalNetAmount.getCurrencyCode();
    }

    public UUID getAssignorId() {
        return assignorId;
    }

    public Instant getSettlementDateTime() {
        return settlementDateTime;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public Money getTotalFaceValue() {
        return totalFaceValue;
    }

    public Money getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public Money getTotalNetAmount() {
        return totalNetAmount;
    }

}
