package com.credit.engine.application.service.currency;

import com.credit.engine.application.dto.currency.ExchangeRateRequest;
import com.credit.engine.application.dto.currency.ExchangeRateResponse;

public interface ExchangeRateService {

    ExchangeRateResponse create(ExchangeRateRequest request);

    ExchangeRateResponse findLatestRate(String originCurrencyCode, String destinationCurrencyCode);

}
