package com.credit.engine.application.service.currency;

import com.credit.engine.application.dto.currency.ExchangeRateRequest;
import com.credit.engine.application.dto.currency.ExchangeRateResponse;
import com.credit.engine.domain.model.currency.ExchangeRate;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.mapper.currency.ExchangeRateMapper;
import com.credit.engine.infrastructure.persistence.repository.currency.CurrencyRepository;
import com.credit.engine.infrastructure.persistence.repository.currency.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateMapper exchangeRateMapper;


    @Override
    @Transactional
    public ExchangeRateResponse create(ExchangeRateRequest request) {
        String origin = request.originCurrencyCode().toUpperCase();
        String destination = request.destinationCurrencyCode().toUpperCase();

        requireCurrencyExists(origin);
        requireCurrencyExists(destination);

        ExchangeRate domain = ExchangeRate.create(origin, destination, request.rate(), request.rateDateTime());

        var saved = exchangeRateRepository.save(exchangeRateMapper.toEntity(domain));

        return ExchangeRateResponse.toResponse(exchangeRateMapper.toDomain(saved));
    }

    @Override
    public ExchangeRateResponse findLatestRate(String originCurrencyCode, String destinationCurrencyCode) {
        var entity = exchangeRateRepository.findFirstByOriginCurrencyIdAndDestinationCurrencyIdOrderByRateDateTimeDesc(
                        originCurrencyCode.toUpperCase(), destinationCurrencyCode.toUpperCase()
                )
                .orElseThrow(() -> new DomainNotFoundException(
                        "Nenhuma cotação encontrada para %s/%s".formatted(originCurrencyCode, destinationCurrencyCode)
                ));

        return ExchangeRateResponse.toResponse(exchangeRateMapper.toDomain(entity));
    }

    private void requireCurrencyExists(String code) {
        if (!currencyRepository.existsById(code))
            throw new DomainNotFoundException("Moeda não encontrada: " + code);
    }
}
