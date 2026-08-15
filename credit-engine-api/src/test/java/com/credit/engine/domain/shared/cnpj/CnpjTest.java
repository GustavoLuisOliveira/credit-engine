package com.credit.engine.domain.shared.cnpj;

import com.credit.engine.domain.shared.exception.InvalidCnpjException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CnpjTest {

    @Test
    @DisplayName("Deve aceitar um CNPJ válido sem máscara e manter apenas os dígitos")
    void shouldAcceptValidCnpjWithoutMask() {
        Cnpj cnpj = Cnpj.of("11222333000181");

        assertThat(cnpj.digits()).isEqualTo("11222333000181");
    }

    @Test
    @DisplayName("Deve aceitar um CNPJ válido com máscara, normalizar para números e formatar corretamente")
    void shouldAcceptValidCnpjWithMaskAndNormalize() {
        Cnpj cnpj = Cnpj.of("11.222.333/0001-81");

        assertThat(cnpj.digits()).isEqualTo("11222333000181");
        assertThat(cnpj.formatted()).isEqualTo("11.222.333/0001-81");
    }

    @Test
    @DisplayName("Deve lançar InvalidCnpjException quando os dígitos verificadores (DV) forem inválidos")
    void shouldRejectCnpjWithInvalidCheckDigits() {
        assertThatThrownBy(() -> Cnpj.of("11222333000199"))
                .isInstanceOf(InvalidCnpjException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"11111111111111", "00000000000000", "99999999999999"})
    @DisplayName("Deve lançar InvalidCnpjException para sequências de dígitos todos iguais")
    void shouldRejectCnpjWithAllSameDigits(String repeatedDigits) {
        assertThatThrownBy(() -> Cnpj.of(repeatedDigits))
                .isInstanceOf(InvalidCnpjException.class);
    }

    @Test
    @DisplayName("Deve rejeitar a criação quando o valor for um CPF válido ao invés de um CNPJ")
    void shouldRejectCpfInput() {
        // CPF válido (11 dígitos) não deve ser aceito neste domínio: só CNPJ.
        assertThatThrownBy(() -> Cnpj.of("123.456.789-01"))
                .isInstanceOf(InvalidCnpjException.class);
    }

    @Test
    @DisplayName("Deve lançar InvalidCnpjException quando o valor fornecido for nulo, vazio ou contiver apenas espaços")
    void shouldRejectNullOrBlankValue() {
        assertThatThrownBy(() -> Cnpj.of(null))
                .isInstanceOf(InvalidCnpjException.class);
        assertThatThrownBy(() -> Cnpj.of("   "))
                .isInstanceOf(InvalidCnpjException.class);
    }

    @Test
    @DisplayName("Deve considerar dois objetos Cnpj como iguais se o valor numérico subjacente for o mesmo")
    void shouldBeEqualWhenCnpjValuesAreSame() {
        assertThat(Cnpj.of("11222333000181")).isEqualTo(Cnpj.of("11.222.333/0001-81"));
    }

}