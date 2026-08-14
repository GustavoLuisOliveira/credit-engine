package com.credit.engine.domain.model.currency;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Modelo de domínio de Currency — POJO puro, sem anotação de persistência.
 * Protege sua própria invariante (código ISO 4217 de 3 letras).
 */
public class Currency {

    private final String code;
    private String name;
    private String symbol;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;


    private Currency(String code, String name, String symbol, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.code = code;
        this.name = Objects.requireNonNull(name, "name é obrigatório");
        this.symbol = Objects.requireNonNull(symbol, "symbol é obrigatório");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Cria uma nova moeda (ainda não persistida), validando o código ISO. */
    public static Currency create(String code, String name, String symbol) {
        String normalizedCode = validateAndNormalizeCode(code);
        return new Currency(normalizedCode, name, symbol, null, null);
    }

    /** Reidrata uma moeda já persistida, sem revalidar o código. */
    public static Currency restore(String code, String name, String symbol, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return new Currency(code, name, symbol, createdAt, updatedAt);
    }

    private static String validateAndNormalizeCode(String code) {
        if (code == null || code.trim().length() != 3)
            throw new IllegalArgumentException("Código de moeda deve ter exatamente 3 letras (ISO 4217)");

        return code.trim().toUpperCase();
    }

    public void rename(String newName, String newSymbol) {
        this.name = Objects.requireNonNull(newName, "name é obrigatório");
        this.symbol = Objects.requireNonNull(newSymbol, "symbol é obrigatório");
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency other)) return false;
        return code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

}
