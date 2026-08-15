package com.credit.engine.domain.model.assignor;

import com.credit.engine.domain.shared.entity.BaseDomainModel;
import com.credit.engine.domain.shared.entity.Cnpj;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de domínio do Cedente (Assignor).
 */
public final class Assignor extends BaseDomainModel {

    private final Cnpj documentNumber;
    private final String name;
    private final String email;
    private final String phone;

    private Assignor(UUID id, Cnpj documentNumber, String name, String email, String phone, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.documentNumber = Objects.requireNonNull(documentNumber, "documentNumber é obrigatório");
        this.name = Objects.requireNonNull(name, "name é obrigatório");
        this.email = Objects.requireNonNull(email, "email é obrigatório");
        this.phone = phone;
    }

    public static Assignor create(Cnpj documentNumber, String name, String email, String phone) {
        return new Assignor(null, documentNumber, name, email, phone, null, null);
    }

    public Assignor update(String name, String email, String phone) {
        return new Assignor(
                this.getId(),
                this.documentNumber, // Mantém o CNPJ original intacto
                name,
                email,
                phone,
                this.getCreatedAt(),
                Instant.now()
        );
    }

    public static Assignor restore(UUID id, Cnpj documentNumber, String name, String email, String phone, Instant createdAt, Instant updatedAt) {
        return new Assignor(id, documentNumber, name, email, phone, createdAt, updatedAt);
    }

    public Cnpj getDocumentNumber() {
        return documentNumber;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assignor other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentNumber);
    }

}
