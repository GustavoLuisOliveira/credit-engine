package com.credit.engine.infrastructure.persistence.repository.receivable;

import com.credit.engine.infrastructure.persistence.entity.receivable.ReceivableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReceivableRepository extends JpaRepository<ReceivableEntity, UUID> {

    /** Apoiada pelo índice idx_receivable_assignor criado na migration V4. */
    List<ReceivableEntity> findByAssignorId(UUID assignorId);

}
