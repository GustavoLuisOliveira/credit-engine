package com.credit.engine.domain.model.receivable;

import com.credit.engine.domain.shared.exception.DomainConflictException;
import com.credit.engine.domain.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReceivableTest {

    private final UUID assignorId = UUID.randomUUID();
    private final Money faceValue = Money.of(new BigDecimal("1000.00"), "BRL");
    private final LocalDate dueDate = LocalDate.now().plusMonths(1);

    @Test
    @DisplayName("Deve criar um título no estado inicial pendente de liquidação (UNSETTLED)")
    void shouldCreateReceivableAsUnsettled() {
        Receivable receivable = Receivable.create(
                assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001", faceValue, dueDate
        );

        assertThat(receivable.getStatus()).isEqualTo(ReceivableStatus.UNSETTLED);
        assertThat(receivable.isSettled()).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar a criação de título com número do documento em branco ou vazio")
    void shouldRejectBlankDocumentNumber() {
        assertThatThrownBy(() -> Receivable.create(
                assignorId, ReceivableType.COMMERCIAL_INVOICE, " ", faceValue, dueDate))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve rejeitar a criação de título sem associar um cedente (assignorId nulo)")
    void shouldRejectNullAssignor() {
        assertThatThrownBy(() -> Receivable.create(null, ReceivableType.COMMERCIAL_INVOICE, "NF-001", faceValue, dueDate))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Deve rejeitar a criação de título com valor de face não positivo (zero ou negativo)")
    void shouldRejectNonPositiveFaceValue() {
        Money zero = Money.zero("BRL");

        assertThatThrownBy(() -> Receivable.create(assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-001", zero, dueDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    @DisplayName("Deve marcar o título como liquidado (SETTLED) a partir do estado pendente (UNSETTLED)")
    void shouldMarkAsSettledFromUnsettled() {
        Receivable receivable = Receivable.create(
                assignorId, ReceivableType.POST_DATED_CHECK, "CHK-001", faceValue, dueDate);

        receivable.markAsSettled();

        assertThat(receivable.isSettled()).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar a tentativa de liquidação de um título que já está liquidado")
    void shouldRejectSettlingAlreadySettledReceivable() {
        Receivable receivable = Receivable.create(assignorId, ReceivableType.POST_DATED_CHECK, "CHK-002", faceValue, dueDate);
        receivable.markAsSettled();

        assertThatThrownBy(receivable::markAsSettled)
                .isInstanceOf(DomainConflictException.class);
    }

    @Test
    @DisplayName("Deve rejeitar o cancelamento de um título que já foi liquidado")
    void shouldRejectCancelingSettledReceivable() {
        Receivable receivable = Receivable.create(assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-002", faceValue, dueDate);
        receivable.markAsSettled();

        assertThatThrownBy(receivable::cancel)
                .isInstanceOf(DomainConflictException.class);
    }

    @Test
    @DisplayName("Deve rejeitar a liquidação de um título previamente cancelado")
    void shouldRejectSettlingCancelledReceivable() {
        Receivable receivable = Receivable.create(assignorId, ReceivableType.COMMERCIAL_INVOICE, "NF-003", faceValue, dueDate);
        receivable.cancel();

        assertThatThrownBy(receivable::markAsSettled)
                .isInstanceOf(DomainConflictException.class);
    }
    
}