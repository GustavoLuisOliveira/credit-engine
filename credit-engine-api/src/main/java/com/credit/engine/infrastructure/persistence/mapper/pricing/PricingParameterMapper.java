package com.credit.engine.infrastructure.persistence.mapper.pricing;

import com.credit.engine.domain.model.pricing.PricingParameter;
import com.credit.engine.infrastructure.persistence.entity.pricing.PricingParameterEntity;
import org.springframework.stereotype.Component;

@Component
public class PricingParameterMapper {

    public PricingParameterEntity toEntity(PricingParameter domain) {
        return new PricingParameterEntity(
                domain.getReceivableType(), domain.getBaseRate(), domain.getSpreadRate(), domain.getEffectiveDate()
        );
    }

    public PricingParameter toDomain(PricingParameterEntity entity) {
        return PricingParameter.restore(
                entity.getId(),
                entity.getReceivableType(),
                entity.getBaseRate(),
                entity.getSpreadRate(),
                entity.getEffectiveDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
