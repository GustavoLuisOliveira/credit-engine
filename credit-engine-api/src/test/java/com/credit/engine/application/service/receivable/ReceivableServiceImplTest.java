package com.credit.engine.application.service.receivable;

import com.credit.engine.application.dto.receivable.ReceivableRequest;
import com.credit.engine.application.dto.receivable.ReceivableResponse;
import com.credit.engine.domain.model.receivable.ReceivableStatus;
import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.domain.shared.exception.DomainConflictException;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.entity.receivable.ReceivableEntity;
import com.credit.engine.infrastructure.persistence.mapper.receivable.ReceivableMapper;
import com.credit.engine.infrastructure.persistence.repository.assignor.AssignorRepository;
import com.credit.engine.infrastructure.persistence.repository.receivable.ReceivableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceivableServiceImplTest {

    @Mock
    private ReceivableRepository receivableRepository;

    @Mock
    private AssignorRepository assignorRepository;

    private final ReceivableMapper receivableMapper = new ReceivableMapper();

    private ReceivableServiceImpl receivableService;

    private final UUID assignorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        receivableService = new ReceivableServiceImpl(receivableRepository, assignorRepository, receivableMapper);
    }

    @Nested
    @DisplayName("Criação de Títulos (create)")
    class Create {

        @Test
        @DisplayName("Deve criar e cadastrar um título com sucesso quando o cedente informado existir no sistema")
        void shouldCreateReceivableWhenAssignorExists() {
            ReceivableRequest request = new ReceivableRequest(
                    assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001", new BigDecimal("1000.00"), "brl", LocalDate.now().plusMonths(1)
            );
            when(assignorRepository.existsById(assignorId)).thenReturn(true);
            when(receivableRepository.save(any(ReceivableEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ReceivableResponse response = receivableService.create(request);

            assertThat(response.currencyCode()).isEqualTo("BRL");
            assertThat(response.status()).isEqualTo(ReceivableStatus.UNSETTLED);
            verify(receivableRepository).save(any(ReceivableEntity.class));
        }

        @Test
        @DisplayName("Deve rejeitar o cadastro de um título e lançar DomainNotFoundException quando o cedente informado não existir")
        void shouldRejectCreationWhenAssignorDoesNotExist() {
            ReceivableRequest request = new ReceivableRequest(
                    assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001", new BigDecimal("1000.00"), "BRL", LocalDate.now().plusMonths(1)
            );
            when(assignorRepository.existsById(assignorId)).thenReturn(false);

            assertThatThrownBy(() -> receivableService.create(request))
                    .isInstanceOf(DomainNotFoundException.class);

            verify(receivableRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Atualização de Títulos (update)")
    class Update {

        @Test
        @DisplayName("Deve atualizar os dados editáveis de um título com sucesso, mantendo cedente e tipo originais")
        void shouldUpdateReceivableSuccessfully() {
            UUID id = UUID.randomUUID();
            ReceivableEntity existingEntity = new ReceivableEntity(
                    id, assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001", new BigDecimal("1000.0000"), "BRL", LocalDate.now().plusMonths(1), ReceivableStatus.UNSETTLED
            );
            ReceivableRequest request = new ReceivableRequest(
                    assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001-REV", new BigDecimal("1200.00"), "brl", LocalDate.now().plusMonths(2)
            );
            when(receivableRepository.findById(id)).thenReturn(Optional.of(existingEntity));
            when(receivableRepository.save(any(ReceivableEntity.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ReceivableResponse response = receivableService.update(id, request);

            assertThat(response.documentNumber()).isEqualTo("NF-001-REV");
            assertThat(response.faceValue()).isEqualByComparingTo("1200.0000");
            assertThat(response.currencyCode()).isEqualTo("BRL");
            verify(receivableRepository).save(any(ReceivableEntity.class));
        }

        @Test
        @DisplayName("Deve lançar DomainNotFoundException ao tentar atualizar um título inexistente")
        void shouldThrowWhenUpdatingNonExistentReceivable() {
            UUID id = UUID.randomUUID();
            ReceivableRequest request = new ReceivableRequest(
                    assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001", new BigDecimal("1000.00"), "BRL", LocalDate.now().plusMonths(1)
            );
            when(receivableRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> receivableService.update(id, request))
                    .isInstanceOf(DomainNotFoundException.class);

            verify(receivableRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar DomainConflictException ao tentar alterar o cedente de um título já cadastrado")
        void shouldRejectUpdateWhenAssignorChanges() {
            UUID id = UUID.randomUUID();
            UUID anotherAssignorId = UUID.randomUUID();
            ReceivableEntity existingEntity = new ReceivableEntity(
                    id, assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001", new BigDecimal("1000.0000"), "BRL", LocalDate.now().plusMonths(1), ReceivableStatus.UNSETTLED
            );
            ReceivableRequest request = new ReceivableRequest(
                    anotherAssignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001", new BigDecimal("1000.00"), "BRL", LocalDate.now().plusMonths(1)
            );
            when(receivableRepository.findById(id)).thenReturn(Optional.of(existingEntity));

            assertThatThrownBy(() -> receivableService.update(id, request))
                    .isInstanceOf(DomainConflictException.class);

            verify(receivableRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar DomainConflictException ao tentar alterar o tipo de um título já cadastrado")
        void shouldRejectUpdateWhenTypeChanges() {
            UUID id = UUID.randomUUID();
            ReceivableEntity existingEntity = new ReceivableEntity(
                    id, assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001", new BigDecimal("1000.0000"), "BRL", LocalDate.now().plusMonths(1), ReceivableStatus.UNSETTLED
            );
            ReceivableRequest request = new ReceivableRequest(
                    assignorId, ReceivableType.POST_DATED_CHECK, "NF-001", new BigDecimal("1000.00"), "BRL", LocalDate.now().plusMonths(1)
            );
            when(receivableRepository.findById(id)).thenReturn(Optional.of(existingEntity));

            assertThatThrownBy(() -> receivableService.update(id, request))
                    .isInstanceOf(DomainConflictException.class);

            verify(receivableRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar DomainConflictException ao tentar atualizar um título que já foi liquidado")
        void shouldRejectUpdateWhenReceivableAlreadySettled() {
            UUID id = UUID.randomUUID();
            ReceivableEntity existingEntity = new ReceivableEntity(
                    id, assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001", new BigDecimal("1000.0000"), "BRL", LocalDate.now().plusMonths(1), ReceivableStatus.SETTLED
            );
            ReceivableRequest request = new ReceivableRequest(
                    assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001-REV", new BigDecimal("1200.00"), "BRL", LocalDate.now().plusMonths(2)
            );
            when(receivableRepository.findById(id)).thenReturn(Optional.of(existingEntity));

            assertThatThrownBy(() -> receivableService.update(id, request))
                    .isInstanceOf(DomainConflictException.class);

            verify(receivableRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Consulta por Identificador (findById)")
    class FindById {

        @Test
        @DisplayName("Deve lançar DomainNotFoundException ao tentar buscar um título por ID inexistente")
        void shouldThrowWhenReceivableNotFound() {
            UUID id = UUID.randomUUID();
            when(receivableRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> receivableService.findById(id))
                    .isInstanceOf(DomainNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Consulta por Cedente (findByAssignor)")
    class FindByAssignor {

        @Test
        @DisplayName("Deve listar todos os títulos/recebíveis vinculados a um cedente específico")
        void shouldListReceivablesByAssignor() {
            ReceivableEntity entity = new ReceivableEntity(
                    UUID.randomUUID(), assignorId, ReceivableType.POST_DATED_CHECK, "CHK-001", new BigDecimal("500.0000"), "USD", LocalDate.now().plusMonths(2), ReceivableStatus.UNSETTLED
            );
            when(receivableRepository.findByAssignorId(assignorId)).thenReturn(List.of(entity));

            List<ReceivableResponse> result = receivableService.findByAssignor(assignorId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).documentNumber()).isEqualTo("CHK-001");
        }
    }

}