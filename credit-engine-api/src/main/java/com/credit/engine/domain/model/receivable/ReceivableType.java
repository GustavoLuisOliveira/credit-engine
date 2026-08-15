package com.credit.engine.domain.model.receivable;

/**
 * Tipo do título.
 * Determina qual PricingStrategy será acionada pelo Spring Component/Registry de precificação
 */
public enum ReceivableType {
    COMMERCIAL_INVOICE,
    POST_DATED_CHECK
}
