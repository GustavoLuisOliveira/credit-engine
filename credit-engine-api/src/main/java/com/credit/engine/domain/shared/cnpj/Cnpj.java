package com.credit.engine.domain.shared.cnpj;

import com.credit.engine.domain.shared.exception.InvalidCnpjException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa um CNPJ válido.
 * Valida não apenas o formato (14 dígitos), mas também os dígitos
 * verificadores (algoritmo oficial da Receita Federal), evitando que
 * sequências como "11111111111111" sejam aceitas apenas por "parecerem" um CNPJ.
 */
public final class Cnpj {

    private static final Pattern NON_DIGITS = Pattern.compile("\\D");
    private static final int[] FIRST_DIGIT_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] SECOND_DIGIT_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private final String value; // sempre 14 dígitos, sem máscara

    private Cnpj(String value) {
        this.value = value;
    }

    public static Cnpj of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidCnpjException("CNPJ não pode ser vazio.");
        }

        String digitsOnly = NON_DIGITS.matcher(raw).replaceAll("");

        if (digitsOnly.length() != 14) {
            throw new InvalidCnpjException("CNPJ deve conter 14 dígitos: " + raw);
        }
        if (isAllSameDigit(digitsOnly)) {
            throw new InvalidCnpjException("CNPJ inválido: " + raw);
        }
        if (!hasValidCheckDigits(digitsOnly)) {
            throw new InvalidCnpjException("CNPJ com dígitos verificadores inválidos: " + raw);
        }

        return new Cnpj(digitsOnly);
    }

    private static boolean isAllSameDigit(String digits) {
        return digits.chars().distinct().count() == 1;
    }

    private static boolean hasValidCheckDigits(String digits) {
        String base = digits.substring(0, 12);
        int firstCheckDigit = calculateCheckDigit(base, FIRST_DIGIT_WEIGHTS);
        int secondCheckDigit = calculateCheckDigit(base + firstCheckDigit, SECOND_DIGIT_WEIGHTS);
        String expected = "" + firstCheckDigit + secondCheckDigit;
        return digits.substring(12).equals(expected);
    }

    /**
     * Calcula o dígito verificador usando o algoritmo do Módulo 11.
     *
     * @param base    String contendo a sequência numérica base.
     * @param weights Array contendo os pesos para cada posição do caractere.
     * @return O dígito verificador calculado (int).
     */
    private static int calculateCheckDigit(String base, int[] weights) {
        int sum = 0;

        // Multiplica cada dígito da string pelo seu respectivo peso e acumula no somatório
        for (int i = 0; i < base.length(); i++)
            sum += Character.getNumericValue(base.charAt(i)) * weights[i];

        // Obtém o resto da divisão do somatório por 11
        int remainder = sum % 11;

        // Se o resto for menor que 2 (0 ou 1), o dígito é 0.
        // Caso contrário, o dígito é a diferença entre 11 e o resto (11 - remainder).
        return remainder < 2 ? 0 : 11 - remainder;
    }

    /** CNPJ formatado: 00.000.000/0000-00 (para exibição). */
    public String formatted() {
        return value.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    /** CNPJ apenas com dígitos, como persistido no banco. */
    public String digits() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cnpj other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return formatted();
    }

}
