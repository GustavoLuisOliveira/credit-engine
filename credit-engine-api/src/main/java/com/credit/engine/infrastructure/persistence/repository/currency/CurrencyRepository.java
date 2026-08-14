package com.credit.engine.infrastructure.persistence.repository.currency;

import com.credit.engine.infrastructure.persistence.entity.currency.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<CurrencyEntity, String> {
}
