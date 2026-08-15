package com.credit.engine.application.dto.assignor;

import com.credit.engine.domain.model.assignor.Assignor;

import java.time.Instant;
import java.util.UUID;

public record AssignorResponse(
        UUID id,
        String documentNumber,
        String name,
        String email,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {
    public static AssignorResponse toResponse(Assignor assignor) {
        return new AssignorResponse(
                assignor.getId(),
                assignor.getDocumentNumber().formatted(),
                assignor.getName(),
                assignor.getEmail(),
                assignor.getPhone(),
                assignor.getCreatedAt(),
                assignor.getUpdatedAt()
        );
    }
}
