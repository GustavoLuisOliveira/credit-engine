package com.credit.engine.domain.shared.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public abstract class BaseDomainModel {

    private final UUID id;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    protected BaseDomainModel(UUID id, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
