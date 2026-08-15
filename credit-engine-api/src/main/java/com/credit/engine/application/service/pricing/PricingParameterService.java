package com.credit.engine.application.service.pricing;

import com.credit.engine.application.dto.pricing.PricingParameterRequest;
import com.credit.engine.application.dto.pricing.PricingParameterResponse;
import com.credit.engine.domain.model.receivable.ReceivableType;

import java.util.List;

public interface PricingParameterService {

    PricingParameterResponse create(PricingParameterRequest request);

    PricingParameterResponse findCurrent(ReceivableType receivableType);

    List<PricingParameterResponse> findHistory(ReceivableType receivableType);

}
