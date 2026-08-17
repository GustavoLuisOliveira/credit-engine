package com.credit.engine.application.service.settlement;

import com.credit.engine.application.dto.currency.ExchangeRateResponse;
import com.credit.engine.application.dto.settlement.SettlementRequest;
import com.credit.engine.application.dto.settlement.SettlementResponse;
import com.credit.engine.application.service.currency.ExchangeRateService;
import com.credit.engine.domain.model.pricing.PricingParameter;
import com.credit.engine.domain.model.receivable.Receivable;
import com.credit.engine.domain.model.receivable.ReceivableStatus;
import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.domain.model.settlement.Settlement;
import com.credit.engine.domain.model.settlement.SettlementItem;
import com.credit.engine.domain.princing.PricingResult;
import com.credit.engine.domain.princing.strategy.PricingStrategy;
import com.credit.engine.domain.princing.strategy.PricingStrategyResolver;
import com.credit.engine.domain.shared.exception.DomainConflictException;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.domain.shared.money.Money;
import com.credit.engine.infrastructure.persistence.entity.pricing.PricingParameterEntity;
import com.credit.engine.infrastructure.persistence.entity.receivable.ReceivableEntity;
import com.credit.engine.infrastructure.persistence.entity.settlement.SettlementEntity;
import com.credit.engine.infrastructure.persistence.entity.settlement.SettlementItemEntity;
import com.credit.engine.infrastructure.persistence.mapper.pricing.PricingParameterMapper;
import com.credit.engine.infrastructure.persistence.mapper.receivable.ReceivableMapper;
import com.credit.engine.infrastructure.persistence.mapper.settlement.SettlementItemMapper;
import com.credit.engine.infrastructure.persistence.mapper.settlement.SettlementMapper;
import com.credit.engine.infrastructure.persistence.repository.assignor.AssignorRepository;
import com.credit.engine.infrastructure.persistence.repository.currency.CurrencyRepository;
import com.credit.engine.infrastructure.persistence.repository.pricing.PricingParameterRepository;
import com.credit.engine.infrastructure.persistence.repository.receivable.ReceivableRepository;
import com.credit.engine.infrastructure.persistence.repository.settlement.SettlementItemRepository;
import com.credit.engine.infrastructure.persistence.repository.settlement.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceImplTest {

    @Mock
    private AssignorRepository assignorRepository;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private ReceivableRepository receivableRepository;
    @Mock
    private ReceivableMapper receivableMapper;
    @Mock
    private PricingParameterRepository pricingParameterRepository;
    @Mock
    private PricingParameterMapper pricingParameterMapper;
    @Mock
    private PricingStrategyResolver pricingStrategyResolver;
    @Mock
    private ExchangeRateService exchangeRateService;
    @Mock
    private SettlementRepository settlementRepository;
    @Mock
    private SettlementMapper settlementMapper;
    @Mock
    private SettlementItemRepository settlementItemRepository;
    @Mock
    private SettlementItemMapper settlementItemMapper;

    private SettlementServiceImpl settlementService;

    private static final UUID ASSIGNOR_ID = UUID.randomUUID();
    private static final LocalDate VALUATION_DATE = LocalDate.of(2026, 8, 16);
    private static final LocalDate DUE_DATE = VALUATION_DATE.plusDays(45);

    @BeforeEach
    void setUp() {
        settlementService = new SettlementServiceImpl(
                assignorRepository, currencyRepository, receivableRepository, receivableMapper,
                pricingParameterRepository, pricingParameterMapper, pricingStrategyResolver, exchangeRateService,
                settlementRepository, settlementMapper, settlementItemRepository, settlementItemMapper
        );
    }

    @Nested
    @DisplayName("Validações de Pré-condição (execute)")
    class ExecutePreconditions {

        @Test
        @DisplayName("Deve falhar com DomainNotFoundException quando o cedente informado não existir")
        void shouldFailWhenAssignorDoesNotExist() {
            // Arrange: cedente não existe no repositório
            when(assignorRepository.existsById(ASSIGNOR_ID)).thenReturn(false);

            SettlementRequest request = buildRequest("BRL", UUID.randomUUID());

            // Act + Assert: deve falhar antes de tocar em qualquer outra dependência
            assertThatThrownBy(() -> settlementService.execute(request))
                    .isInstanceOf(DomainNotFoundException.class)
                    .hasMessageContaining("Cedente não encontrado");

            // Nenhum outro repositório deve ter sido chamado (fail-fast)
            verifyNoInteractions(currencyRepository, receivableRepository, settlementRepository);
        }

        @Test
        @DisplayName("Deve falhar com DomainNotFoundException quando a moeda alvo não existir")
        void shouldFailWhenTargetCurrencyDoesNotExist() {
            // Arrange: cedente existe, mas a moeda alvo não
            when(assignorRepository.existsById(ASSIGNOR_ID)).thenReturn(true);
            when(currencyRepository.existsById("BRL")).thenReturn(false);

            SettlementRequest request = buildRequest("BRL", UUID.randomUUID());

            assertThatThrownBy(() -> settlementService.execute(request))
                    .isInstanceOf(DomainNotFoundException.class)
                    .hasMessageContaining("Moeda alvo não encontrada");

            // Não deve nem chegar a buscar os recebíveis
            verifyNoInteractions(receivableRepository, settlementRepository);
        }

        @Test
        @DisplayName("Deve falhar com DomainNotFoundException quando o recebível não existir no repositório")
        void shouldFailWhenReceivableDoesNotExist() {
            UUID receivableId = UUID.randomUUID();
            // Arrange: cedente e moeda OK, mas o recebível não existe
            when(assignorRepository.existsById(ASSIGNOR_ID)).thenReturn(true);
            when(currencyRepository.existsById("BRL")).thenReturn(true);
            when(receivableRepository.findById(receivableId)).thenReturn(Optional.empty());

            SettlementRequest request = buildRequest("BRL", receivableId);

            assertThatThrownBy(() -> settlementService.execute(request))
                    .isInstanceOf(DomainNotFoundException.class)
                    .hasMessageContaining("Recebível não encontrado");
        }

        @Test
        @DisplayName("Deve falhar com DomainConflictException quando o recebível pertencer a outro cedente")
        void shouldFailWhenReceivableBelongsToAnotherAssignor() {
            UUID receivableId = UUID.randomUUID();
            UUID outroAssignorId = UUID.randomUUID();
            // Arrange: o recebível pertence a um cedente diferente do informado na requisição
            Receivable receivable = buildReceivable(receivableId, outroAssignorId, ReceivableType.COMMERCIAL_INVOICE,
                    new BigDecimal("10000.00"), "BRL", ReceivableStatus.UNSETTLED);

            stubAssignorAndCurrencyOk("BRL");
            stubReceivable(receivableId, receivable);

            SettlementRequest request = buildRequest("BRL", receivableId);

            assertThatThrownBy(() -> settlementService.execute(request))
                    .isInstanceOf(DomainConflictException.class)
                    .hasMessageContaining("não pertence ao cedente informado");
        }

        @Test
        @DisplayName("Deve falhar com DomainConflictException quando o recebível não estiver com status UNSETTLED")
        void shouldFailWhenReceivableIsNotUnsettled() {
            UUID receivableId = UUID.randomUUID();
            // Arrange: recebível já está SETTLED, não pode ser liquidado de novo
            Receivable receivable = buildReceivable(receivableId, ASSIGNOR_ID, ReceivableType.COMMERCIAL_INVOICE,
                    new BigDecimal("10000.00"), "BRL", ReceivableStatus.SETTLED);

            stubAssignorAndCurrencyOk("BRL");
            stubReceivable(receivableId, receivable);

            SettlementRequest request = buildRequest("BRL", receivableId);

            assertThatThrownBy(() -> settlementService.execute(request))
                    .isInstanceOf(DomainConflictException.class)
                    .hasMessageContaining("já liquidado ou cancelado");
        }

        @Test
        @DisplayName("Deve falhar com DomainConflictException quando o recebível já possuir um item de liquidação")
        void shouldFailWhenReceivableAlreadyHasSettlementItem() {
            UUID receivableId = UUID.randomUUID();
            Receivable receivable = buildReceivable(receivableId, ASSIGNOR_ID, ReceivableType.COMMERCIAL_INVOICE,
                    new BigDecimal("10000.00"), "BRL", ReceivableStatus.UNSETTLED);

            stubAssignorAndCurrencyOk("BRL");
            stubReceivable(receivableId, receivable);
            // Arrange: pré-checagem otimista encontra um item já existente para esse recebível
            when(settlementItemRepository.existsByReceivableId(receivableId)).thenReturn(true);

            SettlementRequest request = buildRequest("BRL", receivableId);

            assertThatThrownBy(() -> settlementService.execute(request))
                    .isInstanceOf(DomainConflictException.class)
                    .hasMessageContaining("já liquidado");

            // A pré-checagem barra o fluxo antes de resolver preço: nem chega a consultar parâmetro de precificação
            verifyNoInteractions(pricingParameterRepository, pricingStrategyResolver);
        }

        @Test
        @DisplayName("Deve falhar com DomainNotFoundException quando não houver parâmetro de precificação vigente")
        void shouldFailWhenNoEffectivePricingParameterExists() {
            UUID receivableId = UUID.randomUUID();
            Receivable receivable = buildReceivable(receivableId, ASSIGNOR_ID, ReceivableType.COMMERCIAL_INVOICE,
                    new BigDecimal("10000.00"), "BRL", ReceivableStatus.UNSETTLED);

            stubAssignorAndCurrencyOk("BRL");
            stubReceivable(receivableId, receivable);
            when(settlementItemRepository.existsByReceivableId(receivableId)).thenReturn(false);
            // Arrange: nenhum PricingParameter vigente para o tipo do recebível na data de valoração
            when(pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                    ReceivableType.COMMERCIAL_INVOICE, VALUATION_DATE)).thenReturn(Optional.empty());

            SettlementRequest request = buildRequest("BRL", receivableId);

            assertThatThrownBy(() -> settlementService.execute(request))
                    .isInstanceOf(DomainNotFoundException.class)
                    .hasMessageContaining("Nenhum parâmetro de precificação vigente");
        }
    }

    @Nested
    @DisplayName("Execução de Liquidação na Mesma Moeda (Same-Currency)")
    class SameCurrencyExecution {

        @Test
        @DisplayName("Não deve acionar serviço de câmbio e deve utilizar taxa de conversão neutra (1.0000)")
        void shouldExecuteWithoutExchangeRateServiceAndUseNeutralRate() {
            UUID receivableId = UUID.randomUUID();
            // Arrange: recebível em BRL, moeda alvo também BRL, não deve haver conversão
            Receivable receivable = buildReceivable(receivableId, ASSIGNOR_ID, ReceivableType.COMMERCIAL_INVOICE,
                    new BigDecimal("10000.00"), "BRL", ReceivableStatus.UNSETTLED);

            stubAssignorAndCurrencyOk("BRL");
            stubReceivable(receivableId, receivable);
            when(settlementItemRepository.existsByReceivableId(receivableId)).thenReturn(false);

            // Resultado de precificação fixo, retornado pela PricingStrategy mockada
            PricingResult pricingResult = new PricingResult(
                    new BigDecimal("0.015000"), new BigDecimal("0.025000"), new BigDecimal("1.500000"),
                    Money.of(new BigDecimal("150.00"), "BRL"), Money.of(new BigDecimal("9850.00"), "BRL")
            );
            stubPricing(ReceivableType.COMMERCIAL_INVOICE, pricingResult, receivable);

            stubSettlementPersistence();
            stubSettlementItemPersistence();

            // Act
            SettlementRequest request = buildRequest("BRL", receivableId);
            SettlementResponse response = settlementService.execute(request);

            assertThat(response).isNotNull();
            // Confirma que nenhuma cotação foi consultada, já que same-currency não precisa de câmbio
            verifyNoInteractions(exchangeRateService);

            // Captura o SettlementItem real que a service montou, antes de virar entidade
            SettlementItem capturedItem = captureSettlementItem();
            assertThat(capturedItem.getExchangeRateUsed()).isEqualByComparingTo("1.00000000");
            assertThat(capturedItem.getFaceValue().getCurrencyCode()).isEqualTo("BRL");
            assertThat(capturedItem.getSettlementAmount()).isEqualTo(pricingResult.getPresentValue());
            assertThat(capturedItem.getTerm()).isEqualTo(45);
            assertThat(capturedItem.getTermMonths()).isEqualByComparingTo("1.500000");

            // Captura o Settlement real que a service montou, com os totais já calculados
            Settlement capturedSettlement = captureSettlement();
            assertThat(capturedSettlement.getValuationDate()).isEqualTo(VALUATION_DATE);
            assertThat(capturedSettlement.getTotalFaceValue().getAmount()).isEqualByComparingTo("10000.0000");
            assertThat(capturedSettlement.getTotalDiscountAmount().getAmount()).isEqualByComparingTo("150.0000");
            assertThat(capturedSettlement.getTotalNetAmount().getAmount()).isEqualByComparingTo("9850.0000");

            // O recebível precisa ter sido persistido já como SETTLED
            verify(receivableRepository).save(any(ReceivableEntity.class));
            assertThat(receivable.getStatus()).isEqualTo(ReceivableStatus.SETTLED);
        }
    }

    @Nested
    @DisplayName("Execução de Liquidação com Conversão Cambial (Cross-Currency)")
    class CrossCurrencyExecution {

        @Test
        @DisplayName("Deve converter valor de face, deságio e valor presente utilizando a mesma cotação cambial")
        void shouldConvertFaceValueDiscountAndPresentValueUsingSameExchangeRate() {
            UUID receivableId = UUID.randomUUID();
            // Arrange: título em USD, lote sendo liquidado em BRL, precisa de conversão
            Receivable receivable = buildReceivable(receivableId, ASSIGNOR_ID, ReceivableType.COMMERCIAL_INVOICE,
                    new BigDecimal("2000.00"), "USD", ReceivableStatus.UNSETTLED);

            stubAssignorAndCurrencyOk("BRL");
            stubReceivable(receivableId, receivable);
            when(settlementItemRepository.existsByReceivableId(receivableId)).thenReturn(false);

            PricingResult pricingResult = new PricingResult(
                    new BigDecimal("0.015000"), new BigDecimal("0.025000"), new BigDecimal("1.500000"),
                    Money.of(new BigDecimal("100.00"), "USD"), Money.of(new BigDecimal("1900.00"), "USD")
            );
            stubPricing(ReceivableType.COMMERCIAL_INVOICE, pricingResult, receivable);

            // Cotação USD -> BRL usada na conversão
            ExchangeRateResponse rate = new ExchangeRateResponse(
                    UUID.randomUUID(), "USD", "BRL", new BigDecimal("5.00000000"), Instant.now(), Instant.now(), Instant.now()
            );
            when(exchangeRateService.findLatestRate("USD", "BRL")).thenReturn(rate);

            stubSettlementPersistence();
            stubSettlementItemPersistence();

            // Act
            SettlementRequest request = buildRequest("BRL", receivableId);
            settlementService.execute(request);

            // A cotação deve ter sido buscada exatamente uma vez, no par correto
            verify(exchangeRateService).findLatestRate("USD", "BRL");

            SettlementItem capturedItem = captureSettlementItem();
            // O item preserva faceValue/discountAmount/presentValue na moeda ORIGINAL do título (USD)
            assertThat(capturedItem.getFaceValue().getCurrencyCode()).isEqualTo("USD");
            assertThat(capturedItem.getFaceValue().getAmount()).isEqualByComparingTo("2000.0000");
            assertThat(capturedItem.getDiscountAmount().getAmount()).isEqualByComparingTo("100.0000");
            assertThat(capturedItem.getPresentValue().getAmount()).isEqualByComparingTo("1900.0000");
            // Já o settlementAmount vai na moeda alvo (BRL), convertido pela cotação
            assertThat(capturedItem.getSettlementAmount().getCurrencyCode()).isEqualTo("BRL");
            assertThat(capturedItem.getSettlementAmount().getAmount()).isEqualByComparingTo("9500.0000");
            assertThat(capturedItem.getExchangeRateUsed()).isEqualByComparingTo("5.00000000");

            // Os totais do Settlement usam a mesma cotação sobre faceValue e discountAmount,
            // para que a soma feche corretamente já na moeda alvo
            Settlement capturedSettlement = captureSettlement();
            assertThat(capturedSettlement.getTotalFaceValue().getAmount()).isEqualByComparingTo("10000.0000");
            assertThat(capturedSettlement.getTotalDiscountAmount().getAmount()).isEqualByComparingTo("500.0000");
            assertThat(capturedSettlement.getTotalNetAmount().getAmount()).isEqualByComparingTo("9500.0000");
        }
    }

    @Nested
    @DisplayName("Execução de Liquidação em Lote")
    class BatchExecution {

        @Test
        @DisplayName("Deve acumular os totais consolidados corretamente para múltiplos recebíveis")
        void shouldAccumulateTotalsCorrectlyForMultipleReceivables() {
            UUID receivableId1 = UUID.randomUUID();
            UUID receivableId2 = UUID.randomUUID();

            // Dois recebíveis de tipos e moedas diferentes no mesmo lote
            Receivable receivable1 = buildReceivable(receivableId1, ASSIGNOR_ID, ReceivableType.COMMERCIAL_INVOICE,
                    new BigDecimal("10000.00"), "BRL", ReceivableStatus.UNSETTLED);
            Receivable receivable2 = buildReceivable(receivableId2, ASSIGNOR_ID, ReceivableType.POST_DATED_CHECK,
                    new BigDecimal("1000.00"), "USD", ReceivableStatus.UNSETTLED);

            stubAssignorAndCurrencyOk("BRL");
            stubReceivable(receivableId1, receivable1);
            stubReceivable(receivableId2, receivable2);
            when(settlementItemRepository.existsByReceivableId(any())).thenReturn(false);

            // Precificação do recebível 1 (BRL, sem conversão)
            PricingResult result1 = new PricingResult(
                    new BigDecimal("0.015000"), new BigDecimal("0.025000"), new BigDecimal("1.500000"),
                    Money.of(new BigDecimal("150.00"), "BRL"), Money.of(new BigDecimal("9850.00"), "BRL")
            );
            stubPricing(ReceivableType.COMMERCIAL_INVOICE, result1, receivable1);

            // Precificação do recebível 2 (USD, será convertido)
            PricingResult result2 = new PricingResult(
                    new BigDecimal("0.015000"), new BigDecimal("0.030000"), new BigDecimal("1.000000"),
                    Money.of(new BigDecimal("50.00"), "USD"), Money.of(new BigDecimal("950.00"), "USD")
            );
            stubPricing(ReceivableType.POST_DATED_CHECK, result2, receivable2);

            ExchangeRateResponse rate = new ExchangeRateResponse(
                    UUID.randomUUID(), "USD", "BRL", new BigDecimal("5.00000000"), Instant.now(), Instant.now(), Instant.now()
            );
            when(exchangeRateService.findLatestRate("USD", "BRL")).thenReturn(rate);

            stubSettlementPersistence();
            // Cada chamada a saveAndFlush retorna um mock distinto, não importa qual, aqui
            // o teste valida os totais acumulados no Settlement, não os itens individuais
            when(settlementItemMapper.toEntity(any(SettlementItem.class))).thenReturn(mock(SettlementItemEntity.class));
            when(settlementItemRepository.saveAndFlush(any(SettlementItemEntity.class)))
                    .thenReturn(mock(SettlementItemEntity.class));
            when(settlementItemMapper.toDomain(any(SettlementItemEntity.class))).thenReturn(dummySettlementItem());

            // Act: executa o lote com os dois recebíveis
            SettlementRequest request = new SettlementRequest(ASSIGNOR_ID, VALUATION_DATE, "BRL", List.of(receivableId1, receivableId2));
            settlementService.execute(request);

            // item1 (BRL, sem conversão): faceValue 10000, discount 150
            // item2 (USD, convertido a 5.0): faceValue 1000*5=5000, discount 50*5=250, net 950*5=4750
            // total esperado: face=15000, discount=400, net=9850+4750=14600
            Settlement capturedSettlement = captureSettlement();
            assertThat(capturedSettlement.getTotalFaceValue().getAmount()).isEqualByComparingTo("15000.0000");
            assertThat(capturedSettlement.getTotalDiscountAmount().getAmount()).isEqualByComparingTo("400.0000");
            assertThat(capturedSettlement.getTotalNetAmount().getAmount()).isEqualByComparingTo("14600.0000");

            // Ambos os recebíveis devem ter sido persistidos como liquidados
            verify(receivableRepository, times(2)).save(any(ReceivableEntity.class));
        }
    }

    @Nested
    @DisplayName("Tratamento de Concorrência e Integridade de Dados")
    class ConcurrencyAndIntegrity {

        @Test
        @DisplayName("Deve traduzir DataIntegrityViolationException para DomainConflictException quando ocorrer violação de constraint no banco")
        void shouldTranslateConstraintViolationToDomainConflictException() {
            UUID receivableId = UUID.randomUUID();
            Receivable receivable = buildReceivable(receivableId, ASSIGNOR_ID, ReceivableType.COMMERCIAL_INVOICE,
                    new BigDecimal("10000.00"), "BRL", ReceivableStatus.UNSETTLED);

            stubAssignorAndCurrencyOk("BRL");
            stubReceivable(receivableId, receivable);
            // Pré-checagem otimista passa (ainda não existe item); a violação real vai
            // acontecer só no INSERT, simulando uma corrida entre duas requisições concorrentes
            when(settlementItemRepository.existsByReceivableId(receivableId)).thenReturn(false);

            PricingResult pricingResult = new PricingResult(
                    new BigDecimal("0.015000"), new BigDecimal("0.025000"), new BigDecimal("1.500000"),
                    Money.of(new BigDecimal("150.00"), "BRL"), Money.of(new BigDecimal("9850.00"), "BRL")
            );
            stubPricing(ReceivableType.COMMERCIAL_INVOICE, pricingResult, receivable);

            SettlementEntity savedSettlementEntity = mock(SettlementEntity.class);
            when(savedSettlementEntity.getId()).thenReturn(UUID.randomUUID());
            when(settlementMapper.toEntity(any(Settlement.class))).thenReturn(mock(SettlementEntity.class));
            when(settlementRepository.save(any(SettlementEntity.class))).thenReturn(savedSettlementEntity);

            // O settlement_item chega a ser preparado, mas o saveAndFlush estoura a violação da UNIQUE
            when(settlementItemMapper.toEntity(any(SettlementItem.class))).thenReturn(mock(SettlementItemEntity.class));
            when(settlementItemRepository.saveAndFlush(any(SettlementItemEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("uq_settlement_item_receivable"));

            SettlementRequest request = buildRequest("BRL", receivableId);

            // A exceção técnica do banco deve virar uma exceção de domínio (409)
            assertThatThrownBy(() -> settlementService.execute(request))
                    .isInstanceOf(DomainConflictException.class)
                    .hasMessageContaining("conflito de concorrência");

            // Como o item não foi persistido, o recebível não pode ter sido marcado como liquidado
            verify(receivableRepository, never()).save(any(ReceivableEntity.class));
        }
    }

    @Nested
    @DisplayName("Consultas de Liquidação (find)")
    class SettlementQueries {

        @Test
        @DisplayName("Deve retornar o cabeçalho da liquidação acompanhado de seus itens ao buscar por ID")
        void shouldReturnSettlementWithItemsWhenFoundById() {
            UUID settlementId = UUID.randomUUID();
            SettlementEntity entity = mock(SettlementEntity.class);
            when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(entity));
            when(settlementMapper.toDomain(entity)).thenReturn(dummySettlement("BRL"));

            // Um item vinculado a esse settlement
            SettlementItemEntity itemEntity = mock(SettlementItemEntity.class);
            when(settlementItemRepository.findBySettlementId(settlementId)).thenReturn(List.of(itemEntity));
            when(settlementItemMapper.toDomain(itemEntity)).thenReturn(dummySettlementItem());

            SettlementResponse response = settlementService.findById(settlementId);

            assertThat(response).isNotNull();
            assertThat(response.items()).hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar DomainNotFoundException ao buscar uma liquidação por ID inexistente")
        void shouldFailWhenSettlementNotFoundById() {
            UUID settlementId = UUID.randomUUID();
            when(settlementRepository.findById(settlementId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> settlementService.findById(settlementId))
                    .isInstanceOf(DomainNotFoundException.class)
                    .hasMessageContaining("Liquidação não encontrada");
        }

        @Test
        @DisplayName("Deve listar todas as liquidações vinculadas a um cedente específico")
        void shouldReturnSettlementListWithItemsByAssignor() {
            SettlementEntity entity = mock(SettlementEntity.class);
            UUID settlementId = UUID.randomUUID();
            when(entity.getId()).thenReturn(settlementId);
            when(settlementRepository.findByAssignorId(ASSIGNOR_ID)).thenReturn(List.of(entity));
            when(settlementMapper.toDomain(entity)).thenReturn(dummySettlement("BRL"));
            // Nenhum item vinculado, lote vazio de itens é um caso válido
            when(settlementItemRepository.findBySettlementId(settlementId)).thenReturn(List.of());

            List<SettlementResponse> responses = settlementService.findByAssignor(ASSIGNOR_ID);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).items()).isEmpty();
        }
    }

// ---------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------

    /** Monta uma requisição padrão de liquidação com um único recebível. */
    private SettlementRequest buildRequest(String targetCurrency, UUID receivableId) {
        return new SettlementRequest(ASSIGNOR_ID, VALUATION_DATE, targetCurrency, List.of(receivableId));
    }

    /** Faz o cedente e a moeda alvo passarem nas validações de pré-condição. */
    private void stubAssignorAndCurrencyOk(String targetCurrency) {
        when(assignorRepository.existsById(ASSIGNOR_ID)).thenReturn(true);
        when(currencyRepository.existsById(targetCurrency)).thenReturn(true);
    }

    /** Constrói um Receivable de domínio pronto para os testes, já com status controlado. */
    private Receivable buildReceivable(UUID id, UUID assignorId, ReceivableType type, BigDecimal faceValueAmount,
                                       String currency, ReceivableStatus status) {
        return Receivable.restore(id, assignorId, type, "DOC-" + id, Money.of(faceValueAmount, currency),
                DUE_DATE, status, Instant.now(), Instant.now());
    }

    /** Liga findById(id) -> entity mock -> receivableMapper.toDomain(entity) -> o domínio informado. */
    private void stubReceivable(UUID id, Receivable receivable) {
        ReceivableEntity entity = mock(ReceivableEntity.class);
        when(receivableRepository.findById(id)).thenReturn(Optional.of(entity));
        when(receivableMapper.toDomain(entity)).thenReturn(receivable);

        // lenient(): esse stub só é exercitado nos testes que chegam até persistItemAndSettleReceivable();
        // em testes que falham antes disso (pré-condições, concorrência), ele fica sem uso.
        lenient().when(receivableMapper.toEntity(receivable)).thenReturn(mock(ReceivableEntity.class));
    }

    /** Liga o parâmetro de precificação vigente e a PricingStrategy para o tipo do recebível informado. */
    private void stubPricing(ReceivableType type, PricingResult result, Receivable receivable) {
        PricingParameterEntity parameterEntity = mock(PricingParameterEntity.class);
        PricingParameter parameter = PricingParameter.restore(
                UUID.randomUUID(), type, new BigDecimal("1.500000"), new BigDecimal("2.500000"),
                VALUATION_DATE.minusDays(1), Instant.now(), Instant.now()
        );

        // Repositório retorna a entidade; mapper traduz para o parâmetro de domínio
        when(pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                type, VALUATION_DATE)).thenReturn(Optional.of(parameterEntity));
        when(pricingParameterMapper.toDomain(parameterEntity)).thenReturn(parameter);

        // A estratégia resolvida para o tipo sempre retorna o PricingResult informado
        PricingStrategy strategy = mock(PricingStrategy.class);
        when(pricingStrategyResolver.resolve(type)).thenReturn(strategy);
        when(strategy.calculate(eq(receivable), any(BigDecimal.class), any(BigDecimal.class), eq(VALUATION_DATE)))
                .thenReturn(result);
    }

    /** Liga o fluxo de persistência do cabeçalho: save() -> entity com id -> toDomain(entity). */
    private void stubSettlementPersistence() {
        SettlementEntity savedEntity = mock(SettlementEntity.class);
        when(savedEntity.getId()).thenReturn(UUID.randomUUID());
        when(settlementMapper.toEntity(any(Settlement.class))).thenReturn(mock(SettlementEntity.class));
        when(settlementRepository.save(any(SettlementEntity.class))).thenReturn(savedEntity);
        when(settlementMapper.toDomain(savedEntity)).thenReturn(dummySettlement("BRL"));
    }

    /** Liga o fluxo de persistência do item: saveAndFlush() -> entity salva -> toDomain(entity). */
    private void stubSettlementItemPersistence() {
        SettlementItemEntity savedItemEntity = mock(SettlementItemEntity.class);
        when(settlementItemMapper.toEntity(any(SettlementItem.class))).thenReturn(mock(SettlementItemEntity.class));
        when(settlementItemRepository.saveAndFlush(any(SettlementItemEntity.class))).thenReturn(savedItemEntity);
        when(settlementItemMapper.toDomain(savedItemEntity)).thenReturn(dummySettlementItem());
    }

    /** Captura o SettlementItem real (domínio) que a service passou para o mapper, antes de virar entidade. */
    private SettlementItem captureSettlementItem() {
        ArgumentCaptor<SettlementItem> captor = ArgumentCaptor.forClass(SettlementItem.class);
        verify(settlementItemMapper, atLeastOnce()).toEntity(captor.capture());
        return captor.getValue();
    }

    /** Captura o Settlement real (domínio) que a service passou para o mapper, antes de virar entidade. */
    private Settlement captureSettlement() {
        ArgumentCaptor<Settlement> captor = ArgumentCaptor.forClass(Settlement.class);
        verify(settlementMapper).toEntity(captor.capture());
        return captor.getValue();
    }

    /** Item de liquidação genérico e válido, usado apenas para satisfazer stubs que não são o foco da asserção. */
    private SettlementItem dummySettlementItem() {
        Money money = Money.of(new BigDecimal("100.00"), "BRL");
        return SettlementItem.restore(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 30, new BigDecimal("1.000000"),
                new BigDecimal("0.015000"), new BigDecimal("0.025000"),
                money, Money.of(new BigDecimal("10.00"), "BRL"), Money.of(new BigDecimal("90.00"), "BRL"),
                BigDecimal.ONE.setScale(8), Money.of(new BigDecimal("90.00"), "BRL"), Instant.now(), Instant.now()
        );
    }

    /** Settlement genérico e válido (totais zerados), usado apenas para satisfazer stubs de resposta. */
    private Settlement dummySettlement(String currency) {
        Money zero = Money.zero(currency);
        return Settlement.restore(UUID.randomUUID(), ASSIGNOR_ID, Instant.now(), VALUATION_DATE, zero, zero, zero, Instant.now(), Instant.now());
    }

}