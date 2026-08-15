package com.credit.engine.domain.model.receivable;

import com.credit.engine.domain.shared.exception.DomainConflictException;
import com.credit.engine.domain.shared.model.BaseDomainModel;
import com.credit.engine.domain.shared.money.Money;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de domínio de Receivable (Título)
 * Protege suas próprias invariantes: valor de face sempre positivo (via Money)
 * e transições de status válidas (UNSETTLED/SETTLED/CANCELLED).
 */
public class Receivable extends BaseDomainModel {

    private final UUID assignorId;
    private final ReceivableType type;
    private final String documentNumber;
    private final Money faceValue;
    private final LocalDate dueDate;
    private ReceivableStatus status;

    public Receivable(UUID id, UUID assignorId, ReceivableType type, String documentNumber, Money faceValue, LocalDate dueDate, ReceivableStatus status, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.assignorId = Objects.requireNonNull(assignorId, "assignorId é obrigatório");
        this.type = Objects.requireNonNull(type, "type é obrigatório");
        this.documentNumber = validateDocumentNumber(documentNumber);
        this.faceValue = validateFaceValue(faceValue);
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate é obrigatório");
        this.status = status;
    }

    /** Cria um novo título (ainda não persistido), sempre iniciando como UNSETTLED. */
    public static Receivable create(UUID assignorId, ReceivableType type, String documentNumber, Money faceValue, LocalDate dueDate) {
        return new Receivable(
                null, assignorId, type, documentNumber, faceValue, dueDate, ReceivableStatus.UNSETTLED, null, null
        );
    }

    /**
     * Atualiza os dados editáveis do título (correção de cadastro).
     * Só é permitido enquanto o título estiver UNSETTLED depois de liquidado ou cancelado,
     * os dados fazem parte do histórico e não podem mais mudar (mesma razão pela qual markAsSettled/cancel protegem o status).
     * assignorId e type são mantidos intactos,
     * assim como documentNumber é mantido intacto no Assignor.update():
     * definem o que o título fundamentalmente É, não são "dados de correção" como valor e vencimento.
     */
    public Receivable update(String documentNumber, Money faceValue, LocalDate dueDate) {
        if (status != ReceivableStatus.UNSETTLED)
            throw new DomainConflictException("Não é possível alterar um título que não está mais UNSETTLED: " + this.getId());

        return new Receivable(
                this.getId(),
                this.assignorId,        // mantém o cedente original intacto
                this.type,               // mantém o tipo original intacto
                validateDocumentNumber(documentNumber),
                validateFaceValue(faceValue),
                Objects.requireNonNull(dueDate, "dueDate é obrigatório"),
                this.status,
                this.getCreatedAt(),
                Instant.now()
        );
    }

    /** Reidrata um título já persistido. */
    public static Receivable restore(UUID id, UUID assignorId, ReceivableType type, String documentNumber, Money faceValue, LocalDate dueDate, ReceivableStatus status, Instant createdAt, Instant updatedAt) {
        Objects.requireNonNull(status, "status é obrigatório ao restaurar um Receivable");
        return new Receivable(
                id, assignorId, type, documentNumber, faceValue, dueDate, status, createdAt, updatedAt
        );
    }

    private static String validateDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank())
            throw new IllegalArgumentException("documentNumber é obrigatório");

        return documentNumber;
    }

    private static Money validateFaceValue(Money faceValue) {
        Objects.requireNonNull(faceValue, "faceValue é obrigatório");
        if (!faceValue.isPositive()) {
            throw new IllegalArgumentException("faceValue deve ser maior que zero");
        }
        return faceValue;
    }

    /** Marca o título como liquidado. Só é permitido a partir do status UNSETTLED. */
    public void markAsSettled() {
        if (status == ReceivableStatus.CANCELLED)
            throw new DomainConflictException("Não é possível liquidar um recebível cancelado: " + this.getId());

        if (status == ReceivableStatus.SETTLED)
            throw new DomainConflictException("Recebível já liquidado: " + this.getId());

        this.status = ReceivableStatus.SETTLED;
    }

    /** Cancela o título. Não é permitido cancelar um título já liquidado. */
    public void cancel() {
        if (status == ReceivableStatus.SETTLED)
            throw new DomainConflictException("Não é possível cancelar um recebível já liquidado: " + this.getId());

        this.status = ReceivableStatus.CANCELLED;
    }

    public boolean isSettled() {
        return status == ReceivableStatus.SETTLED;
    }

    public UUID getAssignorId() {
        return assignorId;
    }

    public ReceivableType getType() {
        return type;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public Money getFaceValue() {
        return faceValue;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public ReceivableStatus getStatus() {
        return status;
    }
}
