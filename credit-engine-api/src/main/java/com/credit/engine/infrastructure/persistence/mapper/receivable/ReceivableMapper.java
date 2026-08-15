package com.credit.engine.infrastructure.persistence.mapper.receivable;

import com.credit.engine.domain.model.receivable.Receivable;
import com.credit.engine.domain.shared.money.Money;
import com.credit.engine.infrastructure.persistence.entity.receivable.ReceivableEntity;
import org.springframework.stereotype.Component;

@Component
public class ReceivableMapper {

    public ReceivableEntity toEntity(Receivable domain) {
        return new ReceivableEntity(
                domain.getId(),
                domain.getAssignorId(),
                domain.getType(),
                domain.getDocumentNumber(),
                domain.getFaceValue().getAmount(),
                domain.getFaceValue().getCurrencyCode(),
                domain.getDueDate(),
                domain.getStatus()
        );
    }

    public Receivable toDomain(ReceivableEntity entity) {
        return Receivable.restore(
                entity.getId(),
                entity.getAssignorId(),
                entity.getReceivableType(),
                entity.getDocumentNumber(),
                Money.of(entity.getFaceValue(), entity.getCurrencyId()),
                entity.getDueDate(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
