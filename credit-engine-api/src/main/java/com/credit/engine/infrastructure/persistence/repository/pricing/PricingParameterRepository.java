package com.credit.engine.infrastructure.persistence.repository.pricing;

import com.credit.engine.domain.model.receivable.ReceivableType;
import com.credit.engine.infrastructure.persistence.entity.pricing.PricingParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricingParameterRepository extends JpaRepository<PricingParameterEntity, UUID> {

    /**
     * Busca a taxa vigente para o tipo de recebível na data de referência.
     * Retorna o registro mais recente com data efetiva menor ou igual à data informada.
     */
    Optional<PricingParameterEntity> findFirstByReceivableTypeAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
            ReceivableType receivableType, LocalDate referenceDate
    );

    List<PricingParameterEntity> findByReceivableTypeOrderByEffectiveDateDescCreatedAtDesc(ReceivableType receivableType);

}
