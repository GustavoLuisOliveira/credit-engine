package com.credit.engine.domain.shared.exception;

/**
 * Lançada quando uma entidade de domínio referenciada não existe.
 * Genérica e compartilhada entre contextos.
 */
public class DomainNotFoundException extends RuntimeException {
    public DomainNotFoundException(String message) {
        super(message);
    }
}
