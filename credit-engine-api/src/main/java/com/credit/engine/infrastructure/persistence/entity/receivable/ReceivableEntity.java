package com.credit.engine.infrastructure.persistence.entity.receivable;

import com.credit.engine.domain.model.receivable.ReceivableStatus;
import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.infrastructure.persistence.entity.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade JPA de Receivable. Mapeamento puro para a tabela `receivable`.
 */
@Getter
@Entity
@Table(name = "receivable")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceivableEntity extends BaseEntity {

    @Column(name = "assignor_id", nullable = false, updatable = false)
    private UUID assignorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "receivable_type", length = 50, nullable = false, updatable = false)
    private ReceivableType receivableType;

    @Column(name = "document_number", length = 50, nullable = false, updatable = false)
    private String documentNumber;

    @Column(name = "face_value", nullable = false, precision = 18, scale = 4, updatable = false)
    private BigDecimal faceValue;

    @Column(name = "currency_id", length = 3, nullable = false, updatable = false)
    private String currencyId;

    @Column(name = "due_date", nullable = false, updatable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReceivableStatus status;

    public ReceivableEntity(UUID id, UUID assignorId, ReceivableType receivableType, String documentNumber, BigDecimal faceValue, String currencyId, LocalDate dueDate, ReceivableStatus status) {
        this.setId(id);
        this.assignorId = assignorId;
        this.receivableType = receivableType;
        this.documentNumber = documentNumber;
        this.faceValue = faceValue;
        this.currencyId = currencyId;
        this.dueDate = dueDate;
        this.status = status;
    }

}
