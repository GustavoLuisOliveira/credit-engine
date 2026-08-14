package com.credit.engine.application.service.currency;

import com.credit.engine.application.dto.currency.CurrencyRequest;
import com.credit.engine.application.dto.currency.CurrencyResponse;

import java.util.List;

public interface CurrencyService {

    CurrencyResponse create(CurrencyRequest request);

    CurrencyResponse findByCode(String code);

    List<CurrencyResponse> findAll();

}
