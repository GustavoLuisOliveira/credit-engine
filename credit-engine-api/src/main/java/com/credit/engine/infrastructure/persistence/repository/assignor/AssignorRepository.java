package com.credit.engine.infrastructure.persistence.repository.assignor;

import com.credit.engine.infrastructure.persistence.entity.assignor.AssignorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssignorRepository extends JpaRepository<AssignorEntity, UUID> {

    Optional<AssignorEntity> findByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumber(String documentNumber);

}
