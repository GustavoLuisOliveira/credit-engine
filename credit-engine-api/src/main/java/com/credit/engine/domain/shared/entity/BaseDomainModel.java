package com.credit.engine.domain.shared.entity;

import java.time.Instant;
import java.util.UUID;

public abstract class BaseDomainModel {

    private final UUID id;
    private final Instant createdAt;
    private final Instant updatedAt;

    protected BaseDomainModel(UUID id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
