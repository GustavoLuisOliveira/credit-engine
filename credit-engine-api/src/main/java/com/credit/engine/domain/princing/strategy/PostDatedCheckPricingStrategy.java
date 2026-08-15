package com.credit.engine.domain.princing.strategy;

import com.credit.engine.domain.model.receivable.ReceivableType;
import org.springframework.stereotype.Component;

/** Spread configurado via pricing_parameter para POST_DATED_CHECK. */
@Component
public class PostDatedCheckPricingStrategy extends AbstractPricingStrategy {
    @Override
    public ReceivableType supports() {
        return ReceivableType.POST_DATED_CHECK;
    }
}
