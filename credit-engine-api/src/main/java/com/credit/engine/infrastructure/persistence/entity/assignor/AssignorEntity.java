package com.credit.engine.infrastructure.persistence.entity.assignor;

import com.credit.engine.infrastructure.persistence.entity.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade JPA de AssignorEntity. Mapeamento puro para a tabela `assignor`.
 */
@Getter
@Entity
@Table(
        name = "assignor",
        uniqueConstraints = @UniqueConstraint(name = "uq_assignor_document_number", columnNames = "document_number")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignorEntity extends BaseEntity {

    @Column(name = "document_number", nullable = false, length = 14)
    private String documentNumber;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    public AssignorEntity(UUID id, String documentNumber, String name, String email, String phone) {
        this.setId(id);
        this.documentNumber = documentNumber;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

}
