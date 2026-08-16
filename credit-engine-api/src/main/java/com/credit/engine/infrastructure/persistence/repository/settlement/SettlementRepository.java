package com.credit.engine.infrastructure.persistence.repository.settlement;

import com.credit.engine.infrastructure.persistence.entity.settlement.SettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<SettlementEntity, UUID> {

    /** Apoiada pelo índice idx_settlement_assignor criado na migration V6. */
    List<SettlementEntity> findByAssignorId(UUID assignorId);

}
