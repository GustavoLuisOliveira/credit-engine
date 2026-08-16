package com.credit.engine.infrastructure.persistence.repository.settlement;

import com.credit.engine.infrastructure.persistence.entity.settlement.SettlementItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SettlementItemRepository extends JpaRepository<SettlementItemEntity, UUID> {

    /** Apoiada pelo índice idx_settlement_item_settlement criado na migration V6. */
    List<SettlementItemEntity> findBySettlementId(UUID settlementId);

    /**
     * Checagem otimista pré-cálculo (fail-fast): a proteção real e definitiva contra
     * liquidação em duplicidade sob concorrência é a UNIQUE constraint em receivable_id
     * (uq_settlement_item_receivable).
     */
    boolean existsByReceivableId(UUID receivableId);

}
