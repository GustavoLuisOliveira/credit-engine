package com.credit.engine.application.service.pricing;

import com.credit.engine.application.dto.currency.ExchangeRateResponse;
import com.credit.engine.application.dto.pricing.PricingSimulationResponse;
import com.credit.engine.application.service.currency.ExchangeRateService;
import com.credit.engine.domain.model.receivable.ReceivableStatus;
import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.domain.princing.strategy.CommercialInvoicePricingStrategy;
import com.credit.engine.domain.princing.strategy.PostDatedCheckPricingStrategy;
import com.credit.engine.domain.princing.strategy.PricingStrategy;
import com.credit.engine.domain.princing.strategy.PricingStrategyResolver;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.entity.pricing.PricingParameterEntity;
import com.credit.engine.infrastructure.persistence.entity.receivable.ReceivableEntity;
import com.credit.engine.infrastructure.persistence.mapper.pricing.PricingParameterMapper;
import com.credit.engine.infrastructure.persistence.mapper.receivable.ReceivableMapper;
import com.credit.engine.infrastructure.persistence.repository.pricing.PricingParameterRepository;
import com.credit.engine.infrastructure.persistence.repository.receivable.ReceivableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingCalculationServiceImplTest {

    @Mock
    private ReceivableRepository receivableRepository;

    @Mock
    private PricingParameterRepository pricingParameterRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    private final ReceivableMapper receivableMapper = new ReceivableMapper();
    private final PricingParameterMapper pricingParameterMapper = new PricingParameterMapper();
    private final PricingStrategyResolver pricingStrategyResolver = new PricingStrategyResolver(
            List.<PricingStrategy>of(new CommercialInvoicePricingStrategy(), new PostDatedCheckPricingStrategy()));

    private PricingCalculationServiceImpl pricingCalculationService;

    @BeforeEach
    void setUp() {
        pricingCalculationService = new PricingCalculationServiceImpl(
                receivableRepository, receivableMapper, pricingParameterRepository,
                pricingParameterMapper, pricingStrategyResolver, exchangeRateService);
    }

    @Test
    @DisplayName("Deve simular a precificação sem conversão cambial quando targetCurrencyCode não é informado")
    void shouldSimulateWithoutCurrencyConversionWhenTargetCurrencyIsNull() {
        UUID receivableId = UUID.randomUUID();
        LocalDate valuationDate = LocalDate.now();
        LocalDate dueDate = valuationDate.plusDays(30);

        ReceivableEntity receivableEntity = new ReceivableEntity(
                UUID.randomUUID(), UUID.randomUUID(), ReceivableType.COMMERCIAL_INVOICE, "NF-001",
                new BigDecimal("10000.0000"), "BRL", dueDate, ReceivableStatus.UNSETTLED
        );

        PricingParameterEntity parameterEntity = new PricingParameterEntity(
                ReceivableType.COMMERCIAL_INVOICE, new BigDecimal("0.10"), new BigDecimal("0.015"), valuationDate
        );

        when(receivableRepository.findById(receivableId)).thenReturn(Optional.of(receivableEntity));
        when(pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                ReceivableType.COMMERCIAL_INVOICE, valuationDate))
                .thenReturn(Optional.of(parameterEntity));

        PricingSimulationResponse response = pricingCalculationService.simulate(receivableId, valuationDate, null);

        assertThat(response.receivableId()).isEqualTo(receivableId);
        assertThat(response.currencyCode()).isEqualTo("BRL");
        assertThat(response.targetCurrencyCode()).isNull();
        assertThat(response.exchangeRateUsed()).isNull();
        assertThat(response.convertedAmount()).isNull();
        verify(exchangeRateService, never()).findLatestRate(anyString(), anyString());
    }

    @Test
    @DisplayName("Não deve buscar cotação quando targetCurrencyCode é igual à moeda original (case-insensitive)")
    void shouldNotConvertWhenTargetCurrencyEqualsOriginalCurrency() {
        UUID receivableId = UUID.randomUUID();
        LocalDate valuationDate = LocalDate.now();
        LocalDate dueDate = valuationDate.plusDays(30);

        ReceivableEntity receivableEntity = new ReceivableEntity(
                UUID.randomUUID(), UUID.randomUUID(), ReceivableType.COMMERCIAL_INVOICE, "NF-001",
                new BigDecimal("10000.0000"), "BRL", dueDate, ReceivableStatus.UNSETTLED
        );

        PricingParameterEntity parameterEntity = new PricingParameterEntity(
                ReceivableType.COMMERCIAL_INVOICE, new BigDecimal("0.10"), new BigDecimal("0.015"), valuationDate
        );

        when(receivableRepository.findById(receivableId)).thenReturn(Optional.of(receivableEntity));
        when(pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                ReceivableType.COMMERCIAL_INVOICE, valuationDate))
                .thenReturn(Optional.of(parameterEntity));

        PricingSimulationResponse response = pricingCalculationService.simulate(receivableId, valuationDate, "brl");

        assertThat(response.targetCurrencyCode()).isNull();
        verify(exchangeRateService, never()).findLatestRate(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve aplicar a conversão cambial sobre o valor presente quando targetCurrencyCode difere da moeda original")
    void shouldApplyCurrencyConversionWhenTargetCurrencyDiffersFromOriginal() {
        UUID receivableId = UUID.randomUUID();
        LocalDate valuationDate = LocalDate.now();
        LocalDate dueDate = valuationDate.plusDays(30);

        ReceivableEntity receivableEntity = new ReceivableEntity(
                UUID.randomUUID(), UUID.randomUUID(), ReceivableType.COMMERCIAL_INVOICE, "NF-001",
                new BigDecimal("10000.0000"), "BRL", dueDate, ReceivableStatus.UNSETTLED
        );

        PricingParameterEntity parameterEntity = new PricingParameterEntity(
                ReceivableType.COMMERCIAL_INVOICE, new BigDecimal("0.10"), new BigDecimal("0.015"), valuationDate
        );

        when(receivableRepository.findById(receivableId)).thenReturn(Optional.of(receivableEntity));
        when(pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                ReceivableType.COMMERCIAL_INVOICE, valuationDate))
                .thenReturn(Optional.of(parameterEntity));

        // presentValue esperado ≈ 8968.6099 (mesma conta do teste de strategy, term=1)
        ExchangeRateResponse rateResponse = mock(ExchangeRateResponse.class);
        when(rateResponse.rate()).thenReturn(new BigDecimal("5.00"));
        when(exchangeRateService.findLatestRate("BRL", "USD")).thenReturn(rateResponse);

        PricingSimulationResponse response = pricingCalculationService.simulate(receivableId, valuationDate, "USD");

        assertThat(response.targetCurrencyCode()).isEqualTo("USD");
        assertThat(response.exchangeRateUsed()).isEqualByComparingTo("5.00");
        // convertedAmount = presentValue * exchangeRateUsed
        assertThat(response.convertedAmount())
                .isEqualByComparingTo(response.presentValue().multiply(new BigDecimal("5.00")));
        verify(exchangeRateService).findLatestRate("BRL", "USD");
    }

    @Test
    @DisplayName("Deve resolver a PostDatedCheckPricingStrategy e aplicar o spread de risco correto para POST_DATED_CHECK")
    void shouldResolvePostDatedCheckStrategyAndApplyItsSpread() {
        UUID receivableId = UUID.randomUUID();
        LocalDate valuationDate = LocalDate.now();
        LocalDate dueDate = valuationDate.plusDays(30);

        ReceivableEntity receivableEntity = new ReceivableEntity(
                UUID.randomUUID(), UUID.randomUUID(), ReceivableType.POST_DATED_CHECK, "CH-001",
                new BigDecimal("10000.0000"), "BRL", dueDate, ReceivableStatus.UNSETTLED
        );

        // Mesmo baseRate do cenário de Duplicata, mas spread de 2,5% (risco de Cheque Pré-datado)
        PricingParameterEntity parameterEntity = new PricingParameterEntity(
                ReceivableType.POST_DATED_CHECK, new BigDecimal("10.00"), new BigDecimal("2.50"), valuationDate
        );

        when(receivableRepository.findById(receivableId)).thenReturn(Optional.of(receivableEntity));
        when(pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                ReceivableType.POST_DATED_CHECK, valuationDate))
                .thenReturn(Optional.of(parameterEntity));

        PricingSimulationResponse response = pricingCalculationService.simulate(receivableId, valuationDate, null);

        // totalRate (fração, interno) = 0.125 (10,00% + 2,50%); presentValue = 10000 / 1.125 ≈ 8888.8889 ; discount ≈ 1111.1111
        assertThat(response.spreadRate()).isEqualByComparingTo("2.50");
        assertThat(response.presentValue().doubleValue()).isCloseTo(8888.8889, within(0.01));
        assertThat(response.discountAmount().doubleValue()).isCloseTo(1111.1111, within(0.01));

        // Confirma que o repository foi consultado com o tipo correto (prova que o resolver
        // não "vazou" pra estratégia errada nem pegou parâmetro do tipo errado)
        verify(pricingParameterRepository).findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                ReceivableType.POST_DATED_CHECK, valuationDate);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o recebível não é encontrado")
    void shouldThrowWhenReceivableNotFound() {
        UUID receivableId = UUID.randomUUID();
        when(receivableRepository.findById(receivableId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pricingCalculationService.simulate(receivableId, LocalDate.now(), null))
                .isInstanceOf(DomainNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando não há parâmetro de precificação vigente para o tipo do recebível")
    void shouldThrowWhenNoPricingParameterConfigured() {
        UUID receivableId = UUID.randomUUID();
        LocalDate valuationDate = LocalDate.now();
        ReceivableEntity receivableEntity = new ReceivableEntity(
                UUID.randomUUID(), UUID.randomUUID(), ReceivableType.COMMERCIAL_INVOICE, "NF-002",
                new BigDecimal("500.0000"), "BRL", valuationDate.plusDays(10), ReceivableStatus.UNSETTLED
        );

        when(receivableRepository.findById(receivableId)).thenReturn(Optional.of(receivableEntity));

        when(pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                ReceivableType.COMMERCIAL_INVOICE, valuationDate))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pricingCalculationService.simulate(receivableId, valuationDate, null))
                .isInstanceOf(DomainNotFoundException.class);
    }
}