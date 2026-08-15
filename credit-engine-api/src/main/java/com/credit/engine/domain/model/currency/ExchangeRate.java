package com.credit.engine.domain.model.currency;

import com.credit.engine.domain.shared.model.BaseDomainModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de domínio de ExchangeRate — POJO puro, protege as invariantes:
 * taxa sempre positiva, moedas de origem e destino sempre distintas.
 */
public class ExchangeRate extends BaseDomainModel {

    private final String originCurrencyCode;
    private final String destinationCurrencyCode;
    private final BigDecimal rate;
    private final Instant rateDateTime;

    private ExchangeRate(UUID id, String originCurrencyCode, String destinationCurrencyCode, BigDecimal rate, Instant rateDateTime, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.originCurrencyCode = Objects.requireNonNull(originCurrencyCode, "originCurrencyCode é obrigatório").toUpperCase();
        this.destinationCurrencyCode = Objects.requireNonNull(destinationCurrencyCode, "destinationCurrencyCode é obrigatório").toUpperCase();
        validateDistinctCurrencies(this.originCurrencyCode, this.destinationCurrencyCode);
        this.rate = validatePositiveRate(rate);
        this.rateDateTime = Objects.requireNonNull(rateDateTime, "rateDateTime é obrigatório");
    }

    public static ExchangeRate create(String originCurrencyCode, String destinationCurrencyCode, BigDecimal rate, Instant rateDateTime) {
        return new ExchangeRate(null, originCurrencyCode, destinationCurrencyCode, rate, rateDateTime, null, null);
    }

    public static ExchangeRate restore(UUID id, String originCurrencyCode, String destinationCurrencyCode, BigDecimal rate, Instant rateDateTime, Instant createdAt, Instant updatedAt) {
        return new ExchangeRate(id, originCurrencyCode, destinationCurrencyCode, rate, rateDateTime, createdAt, updatedAt);
    }

    private static BigDecimal validatePositiveRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Taxa de câmbio deve ser positiva");
        }
        return rate;
    }

    private static void validateDistinctCurrencies(String origin, String destination) {
        if (origin.equals(destination))
            throw new IllegalArgumentException("Moeda de origem e destino devem ser diferentes");
    }

    /** Converte um valor na moeda de origem para a moeda de destino usando esta cotação. */
    public BigDecimal convert(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount é obrigatório");
        return amount.multiply(rate);
    }


    public String getOriginCurrencyCode() {
        return originCurrencyCode;
    }

    public String getDestinationCurrencyCode() {
        return destinationCurrencyCode;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public Instant getRateDateTime() {
        return rateDateTime;
    }

}
