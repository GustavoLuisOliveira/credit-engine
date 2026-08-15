package com.credit.engine.domain.shared.currency;

/**
 * Validação/normalização de código de moeda (ISO 4217).
 */
public final class CurrencyCodeValidator {

    private static final int CODE_LENGTH = 3;

    private CurrencyCodeValidator() {}

    public static String validateAndNormalize(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().length() != CODE_LENGTH)
            throw new IllegalArgumentException("Código de moeda deve ter exatamente 3 letras (ISO 4217)");

        return currencyCode.trim().toUpperCase();
    }

}
