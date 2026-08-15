package com.credit.engine.domain.princing.strategy;

import com.credit.engine.domain.model.receivable.ReceivableType;
import org.springframework.stereotype.Component;

/** Spread configurado via pricing_parameter para COMMERCIAL_INVOICE. */
@Component
public class CommercialInvoicePricingStrategy extends AbstractPricingStrategy {
    @Override
    public ReceivableType supports() {
        return ReceivableType.COMMERCIAL_INVOICE;
    }
}
