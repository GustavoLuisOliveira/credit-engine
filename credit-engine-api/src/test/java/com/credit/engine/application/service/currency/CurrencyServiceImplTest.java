package com.credit.engine.application.service.currency;

import com.credit.engine.application.dto.currency.CurrencyRequest;
import com.credit.engine.application.dto.currency.CurrencyResponse;
import com.credit.engine.domain.shared.exception.DomainConflictException;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.entity.currency.CurrencyEntity;
import com.credit.engine.infrastructure.persistence.mapper.currency.CurrencyMapper;
import com.credit.engine.infrastructure.persistence.repository.currency.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceImplTest {

    @Mock
    private CurrencyRepository currencyRepository;

    private final CurrencyMapper currencyMapper = new CurrencyMapper();

    private CurrencyServiceImpl currencyService;

    @BeforeEach
    void setUp() {
        currencyService = new CurrencyServiceImpl(currencyRepository, currencyMapper);
    }

    @Test
    @DisplayName("Deve criar e persistir uma moeda com sucesso quando o código ISO ainda não estiver cadastrado")
    void shouldCreateCurrencyWhenCodeNotYetRegistered() {
        // Preparação dos dados de entrada e comportamento dos mocks
        CurrencyRequest request = new CurrencyRequest("brl", "Real Brasileiro", "R$");

        // Simula que a moeda com o código ISO normalizado ("BRL") ainda não existe no banco de dados
        when(currencyRepository.existsById("BRL")).thenReturn(false);

        // Simula a persistência no Spring Data JPA, devolvendo a própria entidade recebida no método save
        when(currencyRepository.save(any(CurrencyEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Execução da regra de negócio testada
        CurrencyResponse response = currencyService.create(request);

        // Validação do resultado retornado e das interações com os mocks
        assertThat(response.code()).isEqualTo("BRL");
        assertThat(response.name()).isEqualTo("Real Brasileiro");

        // Garante que o repositório de fato chamou a busca por existência e o método save
        verify(currencyRepository).save(any(CurrencyEntity.class));
    }

    @Test
    @DisplayName("Deve lançar DomainConflictException e não salvar ao tentar cadastrar código de moeda duplicado")
    void shouldRejectDuplicateCurrencyCode() {
        CurrencyRequest request = new CurrencyRequest("BRL", "Real Brasileiro", "R$");

        when(currencyRepository.existsById("BRL")).thenReturn(true);

        assertThatThrownBy(() -> currencyService.create(request))
                .isInstanceOf(DomainConflictException.class);

        verify(currencyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar DomainNotFoundException ao buscar por código de moeda inexistente")
    void shouldThrowWhenCurrencyNotFound() {
        when(currencyRepository.findById("XYZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyService.findByCode("XYZ"))
                .isInstanceOf(DomainNotFoundException.class);
    }


    @Test
    @DisplayName("Deve retornar a lista contendo todas as moedas cadastradas no sistema")
    void shouldListAllCurrencies() {
        CurrencyEntity entity = new CurrencyEntity("USD", "Dólar Americano", "US$");
        when(currencyRepository.findAll()).thenReturn(List.of(entity));

        List<CurrencyResponse> result = currencyService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().code()).isEqualTo("USD");
    }

}