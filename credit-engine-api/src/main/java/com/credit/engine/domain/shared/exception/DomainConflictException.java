package com.credit.engine.domain.shared.exception;

/**
 * Acontece quando uma ação tenta duplicar dados ou violar uma regra do sistema
 * (ex: cadastro de moeda já existente).
 */
public class DomainConflictException extends RuntimeException {
    public DomainConflictException(String message) {
        super(message);
    }
}
