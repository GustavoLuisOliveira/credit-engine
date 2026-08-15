package com.credit.engine.application.service.currency;

import com.credit.engine.application.dto.currency.CurrencyRequest;
import com.credit.engine.application.dto.currency.CurrencyResponse;
import com.credit.engine.domain.model.currency.Currency;
import com.credit.engine.domain.shared.exception.DomainConflictException;
import com.credit.engine.domain.shared.exception.DomainNotFoundException;
import com.credit.engine.infrastructure.persistence.mapper.currency.CurrencyMapper;
import com.credit.engine.infrastructure.persistence.repository.currency.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    @Override
    @Transactional
    public CurrencyResponse create(CurrencyRequest request) {
        Currency domain = Currency.create(
                request.code(), request.name(), request.symbol()
        );

        if (currencyRepository.existsById(domain.getCode()))
            throw new DomainConflictException("Moeda %s já cadastrada.".formatted(domain.getCode()));

        var saved = currencyRepository.save(
                currencyMapper.toEntity(domain)
        );

        return CurrencyResponse.toResponse(
                currencyMapper.toDomain(saved)
        );
    }

    @Override
    public CurrencyResponse findByCode(String code) {
        var entity = currencyRepository.findById(code.toUpperCase())
                .orElseThrow(() -> new DomainNotFoundException("Moeda não encontrada: " + code));

        return CurrencyResponse.toResponse(currencyMapper.toDomain(entity));
    }

    @Override
    public List<CurrencyResponse> findAll() {
        return currencyRepository.findAll().stream()
                .map(currencyMapper::toDomain)
                .map(CurrencyResponse::toResponse)
                .toList();
    }
}
