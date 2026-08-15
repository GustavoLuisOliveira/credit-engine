package com.credit.engine.infrastructure.persistence.entity.currency;

import com.credit.engine.infrastructure.persistence.entity.shared.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidade JPA de Currency. Chave natural (código ISO 4217), não gerada.
 * Mapeamento puro para a tabela `currency`
 * regras de negócio ficam no modelo de domínio (domain.model.currency.Currency), traduzido por CurrencyMapper.
 */
@Getter
@Entity
@Table(name = "currency")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurrencyEntity extends BaseAuditEntity {
    @Id
    @Column(name = "id", length = 3, nullable = false, updatable = false)
    private String id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "symbol", length = 5, nullable = false)
    private String symbol;

    public CurrencyEntity(String id, String name, String symbol) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
    }
}
