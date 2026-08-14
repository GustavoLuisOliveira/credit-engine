package com.credit.engine.application.service.currency;

import com.credit.engine.application.dto.currency.ExchangeRateRequest;
import com.credit.engine.application.dto.currency.ExchangeRateResponse;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.entity.currency.ExchangeRateEntity;
import com.credit.engine.infrastructure.persistence.mapper.currency.ExchangeRateMapper;
import com.credit.engine.infrastructure.persistence.repository.currency.CurrencyRepository;
import com.credit.engine.infrastructure.persistence.repository.currency.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceImplTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    private final ExchangeRateMapper exchangeRateMapper = new ExchangeRateMapper();

    private ExchangeRateServiceImpl exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateService = new ExchangeRateServiceImpl(
                exchangeRateRepository,
                currencyRepository,
                exchangeRateMapper
        );
    }

    @Test
    @DisplayName("Deve cadastrar uma nova taxa de câmbio com sucesso quando as moedas de origem e destino existirem")
    void shouldCreateExchangeRateWhenBothCurrenciesExist() {
        // Configura os dados de entrada e mocka a existência das duas moedas
        ExchangeRateRequest request = new ExchangeRateRequest("USD", "BRL", new BigDecimal("5.20"), OffsetDateTime.now());
        when(currencyRepository.existsById("USD")).thenReturn(true);
        when(currencyRepository.existsById("BRL")).thenReturn(true);
        when(exchangeRateRepository.save(any(ExchangeRateEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Executa a lógica de criação da cotação
        ExchangeRateResponse response = exchangeRateService.create(request);

        // Valida se os campos do DTO retornado correspondem aos dados cadastrados e confirma as interações
        assertThat(response.originCurrencyCode()).isEqualTo("USD");
        assertThat(response.rate()).isEqualByComparingTo("5.20");

        verify(currencyRepository).existsById("USD");
        verify(currencyRepository).existsById("BRL");
        verify(exchangeRateRepository).save(any(ExchangeRateEntity.class));
    }

    @Test
    @DisplayName("Deve rejeitar a criação de taxa de câmbio quando a moeda de origem não existir no sistema")
    void shouldRejectExchangeRateWhenOriginCurrencyDoesNotExist() {
        // Simula a ausência da moeda de origem no banco de dados
        ExchangeRateRequest request = new ExchangeRateRequest("XXX", "BRL", new BigDecimal("5.20"), OffsetDateTime.now());
        when(currencyRepository.existsById("XXX")).thenReturn(false);

        // Garante que DomainNotFoundException seja lançada e impede o salvamento no repositório
        assertThatThrownBy(() -> exchangeRateService.create(request))
                .isInstanceOf(DomainNotFoundException.class);

        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar e retornar a taxa de câmbio mais recente para o par de moedas solicitado")
    void shouldReturnLatestRateForPair() {
        // Mocka o repositório para devolver a cotação mais recente encontrada
        ExchangeRateEntity entity = new ExchangeRateEntity("USD", "BRL", new BigDecimal("5.20"), OffsetDateTime.now());
        when(exchangeRateRepository
                .findFirstByOriginCurrencyIdAndDestinationCurrencyIdOrderByRateDateTimeDesc("USD", "BRL"))
                .thenReturn(Optional.of(entity));

        // Executa a busca da última cotação
        ExchangeRateResponse response = exchangeRateService.findLatestRate("USD", "BRL");

        // Valida o valor retornado da cotação
        assertThat(response.rate()).isEqualByComparingTo("5.20");

        verify(exchangeRateRepository)
                .findFirstByOriginCurrencyIdAndDestinationCurrencyIdOrderByRateDateTimeDesc("USD", "BRL");
    }

    @Test
    @DisplayName("Deve lançar DomainNotFoundException quando não houver cotação cadastrada para o par de moedas informado")
    void shouldThrowWhenNoRateFoundForPair() {
        // Simula busca sem resultados no repositório de cotações
        when(exchangeRateRepository
                .findFirstByOriginCurrencyIdAndDestinationCurrencyIdOrderByRateDateTimeDesc("USD", "EUR"))
                .thenReturn(Optional.empty());

        // Garante o lançamento de exceção de domínio quando não houver taxa cadastrada
        assertThatThrownBy(() -> exchangeRateService.findLatestRate("USD", "EUR"))
                .isInstanceOf(DomainNotFoundException.class);
    }

}