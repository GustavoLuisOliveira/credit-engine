package com.credit.engine.infrastructure.persistence.mapper.settlement;

import com.credit.engine.domain.model.settlement.Settlement;
import com.credit.engine.domain.shared.money.Money;
import com.credit.engine.infrastructure.persistence.entity.settlement.SettlementEntity;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {

    public SettlementEntity toEntity(Settlement domain) {
        return new SettlementEntity(
                domain.getAssignorId(),
                domain.getSettlementDateTime(),
                domain.getValuationDate(),
                domain.getTargetCurrencyCode(),
                domain.getTotalFaceValue().getAmount(),
                domain.getTotalDiscountAmount().getAmount(),
                domain.getTotalNetAmount().getAmount()
        );
    }

    public Settlement toDomain(SettlementEntity entity) {
        return Settlement.restore(
                entity.getId(),
                entity.getAssignorId(),
                entity.getSettlementDateTime(),
                entity.getValuationDate(),
                Money.of(entity.getTotalFaceValue(), entity.getTargetCurrencyId()),
                Money.of(entity.getTotalDiscountAmount(), entity.getTargetCurrencyId()),
                Money.of(entity.getTotalNetAmount(), entity.getTargetCurrencyId()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
