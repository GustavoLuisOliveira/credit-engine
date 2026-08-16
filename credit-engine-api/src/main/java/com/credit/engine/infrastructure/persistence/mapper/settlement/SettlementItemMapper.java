package com.credit.engine.infrastructure.persistence.mapper.settlement;

import com.credit.engine.domain.model.settlement.SettlementItem;
import com.credit.engine.domain.shared.money.Money;
import com.credit.engine.infrastructure.persistence.entity.settlement.SettlementItemEntity;
import org.springframework.stereotype.Component;

@Component
public class SettlementItemMapper {

    public SettlementItemEntity toEntity(SettlementItem domain) {
        return new SettlementItemEntity(
                domain.getSettlementId(),
                domain.getReceivableId(),
                domain.getTerm(),
                domain.getTermMonths(),
                domain.getBaseRate(),
                domain.getSpreadRate(),
                domain.getFaceValue().getCurrencyCode(),
                domain.getFaceValue().getAmount(),
                domain.getDiscountAmount().getAmount(),
                domain.getPresentValue().getAmount(),
                domain.getSettlementAmount().getCurrencyCode(),
                domain.getExchangeRateUsed(),
                domain.getSettlementAmount().getAmount()
        );
    }

    public SettlementItem toDomain(SettlementItemEntity entity) {
        String originalCurrency = entity.getOriginalCurrencyId();
        return SettlementItem.restore(
                entity.getId(),
                entity.getSettlementId(),
                entity.getReceivableId(),
                entity.getTerm(),
                entity.getTermMonths(),
                entity.getBaseRate(),
                entity.getSpreadRate(),
                Money.of(entity.getFaceValue(), originalCurrency),
                Money.of(entity.getDiscountAmount(), originalCurrency),
                Money.of(entity.getPresentValue(), originalCurrency),
                entity.getExchangeRateUsed(),
                Money.of(entity.getSettlementAmount(), entity.getSettlementCurrencyId()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
