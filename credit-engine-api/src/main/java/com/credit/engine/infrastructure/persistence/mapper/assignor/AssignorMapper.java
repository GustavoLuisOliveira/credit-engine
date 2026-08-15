package com.credit.engine.infrastructure.persistence.mapper.assignor;

import com.credit.engine.domain.model.assignor.Assignor;
import com.credit.engine.domain.shared.cnpj.Cnpj;
import com.credit.engine.infrastructure.persistence.entity.assignor.AssignorEntity;
import org.springframework.stereotype.Component;

@Component
public class AssignorMapper {

    public AssignorEntity toEntity(Assignor domain) {
        return new AssignorEntity(
                domain.getId(),
                domain.getDocumentNumber().digits(),
                domain.getName(),
                domain.getEmail(),
                domain.getPhone()
        );
    }

    public Assignor toDomain(AssignorEntity entity) {
        return Assignor.restore(
                entity.getId(),
                Cnpj.of(entity.getDocumentNumber()),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
