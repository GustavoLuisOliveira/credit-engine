package com.credit.engine.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita o preenchimento automático de @CreatedDate/@LastModifiedDate
 * declarados em BaseEntity, via AuditingEntityListener.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
