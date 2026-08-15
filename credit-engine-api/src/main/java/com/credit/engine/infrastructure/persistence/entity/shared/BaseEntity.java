package com.credit.engine.infrastructure.persistence.entity.shared;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Superclasse para entidades padrão que utilizam UUID como chave primária autogerada.
 */
@Setter(AccessLevel.PROTECTED)
@Getter
@MappedSuperclass
public abstract class BaseEntity extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
}
