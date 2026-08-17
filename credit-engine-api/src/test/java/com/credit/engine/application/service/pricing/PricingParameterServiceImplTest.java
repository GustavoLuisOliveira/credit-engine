package com.credit.engine.application.service.pricing;

import com.credit.engine.application.dto.pricing.PricingParameterRequest;
import com.credit.engine.application.dto.pricing.PricingParameterResponse;
import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.entity.pricing.PricingParameterEntity;
import com.credit.engine.infrastructure.persistence.mapper.pricing.PricingParameterMapper;
import com.credit.engine.infrastructure.persistence.repository.pricing.PricingParameterRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingParameterServiceImplTest {

    @Mock
    private PricingParameterRepository pricingParameterRepository;

    private final PricingParameterMapper pricingParameterMapper = new PricingParameterMapper();

    private PricingParameterServiceImpl pricingParameterService;

    @BeforeEach
    void setUp() {
        pricingParameterService = new PricingParameterServiceImpl(pricingParameterRepository, pricingParameterMapper);
    }

    @Test
    @DisplayName("Deve criar um novo parâmetro de precificação com sucesso")
    void shouldCreatePricingParameter() {
        // Dado uma requisição válida para criação de parâmetros de precificação
        PricingParameterRequest request = new PricingParameterRequest(
                ReceivableType.COMMERCIAL_INVOICE, new BigDecimal("0.10"), new BigDecimal("0.015"), LocalDate.now()
        );

        // Simula o salvamento no repositório retornando a própria entidade passada
        when(pricingParameterRepository.save(any(PricingParameterEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Quando o serviço é chamado para criar o parâmetro
        PricingParameterResponse response = pricingParameterService.create(request);

        // Então a resposta deve conter os dados mapeados corretamente
        assertThat(response.receivableType()).isEqualTo(ReceivableType.COMMERCIAL_INVOICE);
        assertThat(response.baseRate()).isEqualByComparingTo("0.10");
    }

    @Test
    @DisplayName("Deve retornar o parâmetro de precificação vigente para o tipo de recebível informado")
    void shouldReturnCurrentParameterWhenExists() {
        // Dado que existe um parâmetro cadastrado no repositório para o tipo COMMERCIAL_INVOICE
        PricingParameterEntity entity = new PricingParameterEntity(
                ReceivableType.COMMERCIAL_INVOICE, new BigDecimal("0.10"), new BigDecimal("0.015"), LocalDate.now()
        );

        when(pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                eq(ReceivableType.COMMERCIAL_INVOICE), any(LocalDate.class)))
                .thenReturn(Optional.of(entity));

        // Quando busca a taxa vigente
        PricingParameterResponse response = pricingParameterService.findCurrent(ReceivableType.COMMERCIAL_INVOICE);

        // Então deve retornar a taxa vigente encontrada
        assertThat(response.spreadRate()).isEqualByComparingTo("0.015");
    }

    @Test
    @DisplayName("Deve lançar DomainNotFoundException quando não houver parâmetro vigente cadastrado para o tipo informado")
    void shouldThrowWhenNoCurrentParameterExists() {
        // Dado que não existe parâmetro cadastrado para o tipo POST_DATED_CHECK na data informada
        when(pricingParameterRepository.findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
                eq(ReceivableType.POST_DATED_CHECK), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // Tentar buscar o parâmetro vigente deve lançar exceção de domínio não encontrado
        assertThatThrownBy(() -> pricingParameterService.findCurrent(ReceivableType.POST_DATED_CHECK))
                .isInstanceOf(DomainNotFoundException.class);
    }

    @Test
    @DisplayName("Deve listar o histórico completo de parâmetros de precificação ordenado por data de vigência decrescente")
    void shouldListHistoryForType() {
        // Dado que existem parâmetros cadastrados para o tipo COMMERCIAL_INVOICE
        PricingParameterEntity entity = new PricingParameterEntity(
                ReceivableType.COMMERCIAL_INVOICE, new BigDecimal("0.10"), new BigDecimal("0.015"), LocalDate.now()
        );

        when(pricingParameterRepository.findByReceivableTypeOrderByEffectiveDateDescCreatedAtDesc(ReceivableType.COMMERCIAL_INVOICE))
                .thenReturn(List.of(entity));

        // Quando busca o histórico do tipo informado
        List<PricingParameterResponse> result = pricingParameterService.findHistory(ReceivableType.COMMERCIAL_INVOICE);

        // Então deve retornar a lista com os registros históricos mapeados
        assertThat(result).hasSize(1);
    }

}