package com.credit.engine.application.service.assignor;

import com.credit.engine.application.dto.assignor.AssignorRequest;
import com.credit.engine.application.dto.assignor.AssignorResponse;
import com.credit.engine.domain.model.assignor.Assignor;
import com.credit.engine.domain.shared.entity.Cnpj;
import com.credit.engine.domain.shared.exception.DomainConflictException;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.entity.assignor.AssignorEntity;
import com.credit.engine.infrastructure.persistence.mapper.assignor.AssignorMapper;
import com.credit.engine.infrastructure.persistence.repository.assignor.AssignorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignorServiceImplTest {

    private static final String VALID_CNPJ_MASKED = "11.222.333/0001-81";
    private static final String VALID_CNPJ_DIGITS = "11222333000181";
    private static final String OTHER_VALID_CNPJ_MASKED = "11.444.777/0001-61";

    @Mock
    private AssignorRepository assignorRepository;

    @Mock
    private AssignorMapper assignorMapper;

    @InjectMocks
    private AssignorServiceImpl assignorService;

    private UUID assignorId;
    private Cnpj cnpj;
    private Assignor persistedAssignor;
    private AssignorEntity assignorEntity;
    private AssignorRequest request;

    @BeforeEach
    void setUp() {
        assignorId = UUID.randomUUID();
        cnpj = Cnpj.of(VALID_CNPJ_MASKED);
        persistedAssignor = Assignor.create(cnpj, "Empresa A", "a@empresa.com", "11999999999");
        assignorEntity = new AssignorEntity(assignorId, VALID_CNPJ_DIGITS, "Empresa A", "a@empresa.com", "11999999999");
        request = new AssignorRequest(VALID_CNPJ_MASKED, "Empresa A", "a@empresa.com", "11999999999");
    }

    @Nested
    @DisplayName("Create Assignor Tests")
    class CreateTests {

        @Test
        @DisplayName("Deve salvar o cedente com sucesso quando o CNPJ não existe na base")
        void shouldSaveAssignorWhenCnpjDoesNotExist() {
            when(assignorRepository.existsByDocumentNumber(VALID_CNPJ_DIGITS)).thenReturn(false);
            when(assignorMapper.toEntity(any(Assignor.class))).thenReturn(assignorEntity);
            when(assignorRepository.save(assignorEntity)).thenReturn(assignorEntity);
            when(assignorMapper.toDomain(assignorEntity)).thenReturn(persistedAssignor);

            AssignorResponse response = assignorService.create(request);

            assertThat(response).isNotNull();
            verify(assignorRepository).existsByDocumentNumber(VALID_CNPJ_DIGITS);
            verify(assignorRepository).save(assignorEntity);
        }

        @Test
        @DisplayName("Deve lançar DomainConflictException quando o CNPJ já estiver cadastrado")
        void shouldThrowDomainConflictExceptionWhenCnpjAlreadyExists() {
            when(assignorRepository.existsByDocumentNumber(VALID_CNPJ_DIGITS)).thenReturn(true);

            assertThatThrownBy(() -> assignorService.create(request))
                    .isInstanceOf(DomainConflictException.class)
                    .hasMessageContaining(cnpj.formatted());

            verify(assignorRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Assignor Tests")
    class UpdateTests {

        @Test
        @DisplayName("Deve atualizar os dados do cedente com sucesso quando o CNPJ for mantido")
        void shouldUpdateAssignorDataWhenCnpjRemainsTheSame() {
            AssignorRequest updateRequest =
                    new AssignorRequest(VALID_CNPJ_MASKED, "Empresa A Ltda", "novo@empresa.com", "11888888888");

            when(assignorRepository.findById(assignorId)).thenReturn(Optional.of(assignorEntity));
            when(assignorMapper.toDomain(assignorEntity)).thenReturn(persistedAssignor);
            when(assignorMapper.toEntity(any(Assignor.class))).thenReturn(assignorEntity);
            when(assignorRepository.save(assignorEntity)).thenReturn(assignorEntity);

            AssignorResponse response = assignorService.update(assignorId, updateRequest);

            assertThat(response).isNotNull();

            ArgumentCaptor<Assignor> captor = ArgumentCaptor.forClass(Assignor.class);
            verify(assignorMapper).toEntity(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("Empresa A Ltda");
            assertThat(captor.getValue().getEmail()).isEqualTo("novo@empresa.com");
            assertThat(captor.getValue().getPhone()).isEqualTo("11888888888");
            assertThat(captor.getValue().getDocumentNumber()).isEqualTo(cnpj);

            verify(assignorRepository).save(assignorEntity);
        }

        @Test
        @DisplayName("Deve lançar DomainConflictException ao tentar alterar o CNPJ do cedente")
        void shouldThrowDomainConflictExceptionWhenAttemptingToChangeCnpj() {
            AssignorRequest updateRequest =
                    new AssignorRequest(OTHER_VALID_CNPJ_MASKED, "Empresa A", "a@empresa.com", null);

            when(assignorRepository.findById(assignorId)).thenReturn(Optional.of(assignorEntity));
            when(assignorMapper.toDomain(assignorEntity)).thenReturn(persistedAssignor);

            assertThatThrownBy(() -> assignorService.update(assignorId, updateRequest))
                    .isInstanceOf(DomainConflictException.class)
                    .hasMessageContaining("CNPJ");

            verify(assignorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar DomainNotFoundException ao tentar atualizar um cedente inexistente")
        void shouldThrowDomainNotFoundExceptionWhenUpdatingNonExistentAssignor() {
            when(assignorRepository.findById(assignorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignorService.update(assignorId, request))
                    .isInstanceOf(DomainNotFoundException.class)
                    .hasMessageContaining(assignorId.toString());

            verify(assignorRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find Assignor By ID Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Deve retornar os dados do cedente quando encontrado pelo ID")
        void shouldReturnAssignorResponseWhenFoundById() {
            when(assignorRepository.findById(assignorId)).thenReturn(Optional.of(assignorEntity));
            when(assignorMapper.toDomain(assignorEntity)).thenReturn(persistedAssignor);

            AssignorResponse response = assignorService.findById(assignorId);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar DomainNotFoundException quando o cedente não for encontrado pelo ID")
        void shouldThrowDomainNotFoundExceptionWhenNotFoundById() {
            when(assignorRepository.findById(assignorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignorService.findById(assignorId))
                    .isInstanceOf(DomainNotFoundException.class)
                    .hasMessageContaining(assignorId.toString());
        }
    }

    @Nested
    @DisplayName("Find Assignor By Document Number Tests")
    class FindByDocumentNumberTests {

        @Test
        @DisplayName("Deve retornar os dados do cedente quando encontrado pelo número de CNPJ")
        void shouldReturnAssignorResponseWhenFoundByDocumentNumber() {
            when(assignorRepository.findByDocumentNumber(VALID_CNPJ_DIGITS)).thenReturn(Optional.of(assignorEntity));
            when(assignorMapper.toDomain(assignorEntity)).thenReturn(persistedAssignor);

            AssignorResponse response = assignorService.findByDocumentNumber(VALID_CNPJ_MASKED);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar DomainNotFoundException quando o cedente não for encontrado pelo CNPJ")
        void shouldThrowDomainNotFoundExceptionWhenNotFoundByDocumentNumber() {
            when(assignorRepository.findByDocumentNumber(VALID_CNPJ_DIGITS)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignorService.findByDocumentNumber(VALID_CNPJ_MASKED))
                    .isInstanceOf(DomainNotFoundException.class)
                    .hasMessageContaining(cnpj.formatted());
        }
    }

}